package cn.edu.fudan.violation.core.analyzer;

import cn.edu.fudan.violation.dao.IssueAnalyzerDao;
import cn.edu.fudan.violation.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.enums.ScanStatusEnum;
import cn.edu.fudan.violation.util.DiffFile;
import cn.edu.fudan.violation.util.FileUtil;
import cn.edu.fudan.violation.util.JGitHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * description: 工具具体的执行流程
 *
 * @author fancying
 * create: 2020-05-20 15:53
 **/
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public abstract class BaseAnalyzer {

    private static final String SEPARATOR = System.getProperty("file.separator");
    @Value("${binHome}")
    protected String binHome;
    @Value("${enable.batch}")
    protected String batch;
    @Value("${copyTempRepoPath}")
    protected String copyTempRepoPath;
    protected List<RawIssue> resultRawIssues = new ArrayList<>();
    protected Map<String, Set<String>> methodsAndFieldsInFile = new HashMap<>(32);
    protected boolean isTotalScan = false;
    Map<String, List<String>> commitToParentCommits = new HashMap<>(4);
    @Value("${debugMode}")
    protected boolean debugMode;

    /**
     * 调用工具扫描
     *
     * @param repoUuid repoUuid
     * @param repoPath repoPath
     * @param commit   commit
     * @return 调用工具是否成功
     */
    public abstract boolean invoke(String repoUuid, String repoPath, String commit);

    /**
     * 调用工具进行解析,如sonarqube结果解析成rawIssue
     *
     * @param repoPath repoPath
     * @param repoUuid repoUuid
     * @param commit   commitId
     * @return 解析是否成功
     */
    public abstract boolean analyze(String repoPath, String repoUuid, String commit, JGitHelper jGitHelper);

    /**
     * 返回工具名
     *
     * @return 工具名
     */
    public abstract String getToolName();

    /**
     * 返回该缺陷的优先级
     *
     * @param rawIssue rawIssue
     * @return 缺陷优先级
     */
    public abstract Integer getPriorityByRawIssue(RawIssue rawIssue);

    /**
     * 根据文件名获取文件中的方法和变量
     *
     * @param fileName fileName
     * @return methods and fields
     */
    public Set<String> getMethodsAndFieldsInFile(String fileName) throws IOException {
        return methodsAndFieldsInFile.getOrDefault(fileName, new HashSet<>());
    }

    public void emptyAnalyzeRawIssues() {
        resultRawIssues.clear();
    }

    /**
     * @param repoUuid        repoUuid
     * @param repoResource    key for JGitHelper value for resource
     * @param commit          commit
     * @param resourcesStatus
     * @param remainingNum
     * @param completeFlag
     * @param lock
     * @param hasResource
     * @param resourceReady
     */
    @Async("produce-resource")
    public void produceResource(String repoUuid, Map<JGitHelper, Boolean> repoResource, String commit, Map<String, ScanStatusEnum> resourcesStatus,
                                AtomicInteger remainingNum, CountDownLatch completeFlag, Lock lock, Condition hasResource, Condition resourceReady,
                                IssueAnalyzerDao issueAnalyzerDao, List<String> commitList, Lock lock1) {
        Thread.currentThread().setName("prc-" + commit.substring(0, 6));
        log.info("1: begin prepare resource repoUuid:{} commit:{}", repoUuid, commit);
        JGitHelper jGitHelper = null;
        lock1.lock();
        for (Map.Entry<JGitHelper, Boolean> jgitEntry : repoResource.entrySet()) {
            if (Boolean.TRUE.equals(jgitEntry.getValue())) {
                jGitHelper = jgitEntry.getKey();
                repoResource.put(jgitEntry.getKey(), false);
                break;
            }
        }
        lock1.unlock();
        // checkout and invoke
        assert jGitHelper != null;

        ScanStatusEnum scanStatusEnum = ScanStatusEnum.CHECKOUT_FAILED;

        //todo 并发情况下此处可能报NLP
        String repoPath = jGitHelper.getRepoPath();
        if (jGitHelper.checkout(commit)) {
            // 根据其parent是否被扫描过确定是全量扫描还是增量扫描
            try {
                String scanRepoPath = getScanRepoPath(repoUuid, commit, jGitHelper, issueAnalyzerDao, commitList);
                if (scanRepoPath != null) {
                    repoPath = scanRepoPath;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            scanStatusEnum = invokeAndAnalyze(repoPath, repoUuid, commit, jGitHelper);
        } else {
            log.debug("checkout failed repo Path is {}", jGitHelper.getRepoPath());
        }

        completeFlag.countDown();
        log.info("2: prepare resource Done! repoUuid:{} commit:{}", repoUuid, commit);

        // 存储资源在数据库中 缓解内存压力 但是会增加查询开销
        IssueAnalyzer issueAnalyzer = IssueAnalyzer.initIssueAnalyze(repoUuid, commit, getToolName());

        log.info("init issueAnalyzer success, begin set status");

        //编译失败的commit，用sonar仍旧可能会有数据得出，所以当编译失败的时候需要传入空数组
        if (!ScanStatusEnum.DONE.equals(scanStatusEnum)) {
            issueAnalyzer.updateIssueAnalyzeStatus(new ArrayList<>());
            issueAnalyzer.setInvokeResult(IssueAnalyzer.InvokeResult.FAILED.getStatus());
        } else {
            issueAnalyzer.updateIssueAnalyzeStatus(resultRawIssues);
            issueAnalyzer.setInvokeResult(IssueAnalyzer.InvokeResult.SUCCESS.getStatus());
            issueAnalyzer.setIsTotalScan(isTotalScan?1:0);
        }

        log.info("set status success, begin insert issueAnalyzer");

        issueAnalyzerDao.insertIssueAnalyzer(issueAnalyzer);
        log.info("3: insert done ! repoUuid:{} commit:{}", repoUuid, commit);

        // 资源准备完成 改变状态
        resourcesStatus.put(commit, scanStatusEnum);
        repoResource.put(jGitHelper, true);
        remainingNum.addAndGet(1);
        log.info("4: resource change Done! repoUuid:{} commit:{}", repoUuid, commit);

        // 资源准备完成一个 通知其他等待线程
        lock.lock();
        try {
            hasResource.signal();
            resourceReady.signalAll();
        } finally {
            lock.unlock();
        }
        log.info("5: signal Done! repoUuid:{} commit:{}", repoUuid, commit);
    }

    public ScanStatusEnum invokeAndAnalyze(String repoPath, String repoUuid, String commit, JGitHelper jGitHelper) {
        //1 invoke tool
        long invokeToolStartTime = System.currentTimeMillis();
        boolean invokeToolResult = invoke(repoUuid, repoPath, commit);
        if (!invokeToolResult) {
            log.info("invoke tool failed!repo path is {}, commit is {}", repoPath, commit);
            return ScanStatusEnum.INVOKE_TOOL_FAILED;
        }
        long invokeToolTime = System.currentTimeMillis();
        log.info("invoke tool use {} s,invoke tool success!", (invokeToolTime - invokeToolStartTime) / 1000);

        //2 analyze raw issues
        boolean analyzeResult = analyze(repoPath, repoUuid, commit, jGitHelper);
        if (!analyzeResult) {
            log.error("analyze raw issues failed!repo path is {}, commit is {}", repoPath, commit);
            return ScanStatusEnum.ANALYZE_FAILED;
        }
        long analyzeToolTime = System.currentTimeMillis();
        log.info("analyze raw issues use {} s, analyze success!", (analyzeToolTime - invokeToolTime) / 1000);

        //增量扫描完成后删除复制的文件
        if (!isTotalScan && !debugMode) {
            //todo
            deleteCopyFiles(repoPath);
        }
        commitToParentCommits.remove(commit);

        // 结果放到了 result rawIssue 中
        return ScanStatusEnum.DONE;
    }

    /**
     * 开启资源提前准备
     *
     * @return 是否开启资源提前准备
     */
    public boolean closeResourceLoader() {
        return false;
    }

    public String getScanRepoPath(String repoUuid, String curCommit, JGitHelper jGitHelper, IssueAnalyzerDao issueAnalyzerDao, List<String> commitList) throws IOException {
        String[] commitParents = jGitHelper.getCommitParents(curCommit);
        String toolName = getToolName();
        if (commitParents.length == 0) {
            commitToParentCommits.put(curCommit, new ArrayList<>());
            isTotalScan = true;
        }
        //如果扫描列表并且cache中不存在parent 才是全量扫描
        for (String commitParent : commitParents) {
            if (!commitList.contains(commitParent) && !issueAnalyzerDao.cached(repoUuid,commitParent,toolName)) {
                isTotalScan = true;
                break;
            }
        }
        if (isTotalScan) {
            return null;
        }
        String targetRepoDir = copyTempRepoPath + SEPARATOR + repoUuid + SEPARATOR + curCommit;
        Set<String> curFiles = new HashSet<>();
        commitToParentCommits.put(curCommit, Arrays.asList(commitParents));
        for (String commitParent : commitParents) {
            String parentBaseDir = targetRepoDir + SEPARATOR + commitParent + SEPARATOR;
            new File(parentBaseDir).mkdirs();
            DiffFile diffFilePair = jGitHelper.getDiffFilePair(commitParent, curCommit);
            List<String> addFiles = diffFilePair.getAddFiles();
            List<String> deleteFiles = diffFilePair.getDeleteFiles();
            Map<String, String> changeFiles = diffFilePair.getChangeFiles();
            List<String> parentFiles = new ArrayList<>();
            parentFiles.addAll(deleteFiles);
            parentFiles.addAll(changeFiles.keySet());
            jGitHelper.checkout(commitParent);
            for (String parentFile : parentFiles) {
                if(!fileFilter(parentFile)){
                    FileUtil.copyFile(jGitHelper.getRepoPath() + SEPARATOR + parentFile, parentBaseDir + parentFile);
                }
            }
            curFiles.addAll(addFiles);
            curFiles.addAll(changeFiles.values());
        }
        jGitHelper.checkout(curCommit);
        String curBaseDir = targetRepoDir + SEPARATOR + curCommit + SEPARATOR;
        new File(curBaseDir).mkdirs();
        for (String curFile : curFiles) {
            if(!fileFilter(curFile)){
                FileUtil.copyFile(jGitHelper.getRepoPath() + SEPARATOR + curFile, curBaseDir + curFile);
            }
        }
        return targetRepoDir;
    }

    private void deleteCopyFiles(String copyTempRepoPath) {
        Runtime rt = Runtime.getRuntime();
        String command = binHome + "deleteCopyFile.sh " + copyTempRepoPath;
        log.info("command -> {}", command);
        try {
            Process process = rt.exec(command);
            boolean timeOut = process.waitFor(20L, TimeUnit.SECONDS);
            if (!timeOut) {
                process.destroy();
                log.error("delete file {} timeout ! (20s)", copyTempRepoPath);
                return;
            }
            log.info("delete file {} success !", copyTempRepoPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //false:不过滤 true:过滤
    public boolean fileFilter(String fileName){
        return false;
    }


}
