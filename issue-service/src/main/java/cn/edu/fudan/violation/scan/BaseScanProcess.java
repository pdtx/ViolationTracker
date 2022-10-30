package cn.edu.fudan.violation.scan;


import cn.edu.fudan.violation.component.RestInterfaceManager;
import cn.edu.fudan.violation.domain.scan.OnceScanInfo;
import cn.edu.fudan.violation.domain.scan.RepoScan;
import cn.edu.fudan.violation.domain.scan.ScanStatus;
import cn.edu.fudan.violation.exception.CodePathGetFailedException;
import cn.edu.fudan.violation.exception.RepoScanGetException;
import cn.edu.fudan.violation.util.JGitHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author fancying
 * @author beethoven
 */
@Getter
@NoArgsConstructor
public abstract class BaseScanProcess implements ScanStatus {

    private static final Logger log = LoggerFactory.getLogger(BaseScanProcess.class);
    private static final String KEY_DELIMITER = "-";

    /**
     * key repoUuid
     * value true/false true 代表还需要更新扫描一次
     */
    private final ConcurrentHashMap<String, Boolean> scanStatusMap = new ConcurrentHashMap<>();
    protected RestInterfaceManager baseRepoRestManager;
    protected ApplicationContext applicationContext;
    private static final Object LOCK = new Object();

    public BaseScanProcess(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        baseRepoRestManager = applicationContext.getBean(RestInterfaceManager.class);
    }

    @Async("taskExecutor")
    public void scan(String repoUuid, String branch, String beginCommit, String endCommit) throws CodePathGetFailedException, RepoScanGetException, IOException {
        String[] tools=getToolsByRepo(repoUuid);
        for (String tool : tools) {
            RepoScan repoScan = getRepoScan(repoUuid, tool, branch);
            boolean isFirstScan = !StringUtils.isEmpty(beginCommit);
            boolean isScanned = repoScan != null;

            // 扫描过并且是第一次扫描说明该请求已经处理过
            if (isScanned && isFirstScan) {
                log.warn("{} : already scanned before", repoUuid);
                return;
            }

            String key = generateKey(repoUuid, tool);
            synchronized (LOCK) {
                // 正在扫描接收到了请求
                if (scanStatusMap.containsKey(key)) {
                    scanStatusMap.put(key, true);
                    return;
                }
                scanStatusMap.putIfAbsent(key, false);
            }
            beginScan(repoUuid, branch, beginCommit, tool, endCommit);
            checkAfterScan(repoUuid, branch, tool, endCommit);
        }
    }

    protected String generateKey(String repoUuid, String tool) {
        return repoUuid + KEY_DELIMITER + tool;
    }

    /**
     * 一个 commitList 扫描完成之后再次检查 查看是否有更新扫描的请求
     **/
    private void checkAfterScan(String repoUuid, String branch, String tool, String endCommit) throws CodePathGetFailedException, RepoScanGetException, IOException {
        String key = generateKey(repoUuid, tool);
        if (!scanStatusMap.containsKey(key)) {
            log.error("{} : not in cn.edu.fudan.common.scan scanStatusMap", repoUuid);
            return;
        }
        synchronized (LOCK) {
            boolean newUpdate = scanStatusMap.get(key);
            if (!newUpdate) {
                scanStatusMap.remove(key);
                return;
            }
            scanStatusMap.put(key, false);
        }
        beginScan(repoUuid, branch, null, tool, endCommit);
        checkAfterScan(repoUuid, branch, tool, endCommit);
    }

    void beginScan(String repoUuid, String branch, String beginCommit, String tool, String endCommit) throws CodePathGetFailedException, RepoScanGetException, IOException {
        Thread curThread = Thread.currentThread();
        String threadName = generateKey(repoUuid, tool);
        curThread.setName(threadName);
        //获取tool scan
        AbstractToolScan specificTool = getToolScan(tool);
        String repoPath = baseRepoRestManager.getCodeServiceRepo(repoUuid);
        log.info("repoPath is {}", repoPath);
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            throw new CodePathGetFailedException("can't get repo path");
        }

        RepoScan repoScan = prepareRepoScan(specificTool, repoUuid, tool, repoPath, branch, beginCommit, endCommit);

        if (repoScan == null) {
            log.info("scan list size is 0, scan finish");
            return;
        }


        try {
            specificTool.prepareForScan();
            for (String commit : specificTool.getScanData().getToScanCommitList()) {
                System.out.println(commit);
                specificTool.prepareForOneScan(commit);
                specificTool.scanOneCommit(commit);
                specificTool.cleanUpForOneScan(commit);
                if (curThread.isInterrupted()) {
                    synchronized (LOCK) {
                        scanStatusMap.remove(threadName);
                    }
                    log.warn("thread:{} stopped", threadName);
                    break;
                }
                repoScan.setEndScanTime(LocalDateTime.now());
                repoScan.setScannedCommitCount(repoScan.getScannedCommitCount() + 1);
                Duration duration = Duration.between(repoScan.getStartScanTime(), repoScan.getEndScanTime());
                repoScan.setScanTime(duration.toMillis() / 1000);
                updateRepoScan(repoScan);
            }
            repoScan.setScanStatus(RepoScan.COMPLETE);
            specificTool.cleanUpForScan();
        } catch (Exception e) {
            e.printStackTrace();
            repoScan.setScanStatus(RepoScan.FAILED);
            repoScan.setEndScanTime(LocalDateTime.now());
            Duration duration = Duration.between(repoScan.getStartScanTime(), repoScan.getEndScanTime());
            repoScan.setScanTime(duration.toMillis() / 1000);
        } finally {
            updateRepoScan(repoScan);
            baseRepoRestManager.freeRepoPath(repoUuid, repoPath);
        }

    }





    private RepoScan prepareRepoScan(AbstractToolScan specificTool, String repoUuid, String tool, String repoPath, String branch, String beginCommit, String endCommit) throws RepoScanGetException {

        log.info("repoPath:{}", repoPath);
        Set<String> scannedCommitList = new HashSet<>(getScannedCommitList(repoUuid, tool));
        log.info("scannedCommitList.size():{}", scannedCommitList.size());
        boolean initialScan = scannedCommitList.isEmpty();

        if (StringUtils.isEmpty(beginCommit)) {
            beginCommit = getLastedScannedCommit(repoUuid, tool);
            if (beginCommit == null) {
                log.error("get begin commit error");
                throw new RepoScanGetException("get begin commit error");
            }
        }
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> toScanCommitList = jGitHelper.getScanCommitListByBranchAndBeginCommit(branch, beginCommit, scannedCommitList);

        //jGitHelper.close();

        // 筛选出end commit
        if (!StringUtils.isEmpty(endCommit)) {
            int end = toScanCommitList.indexOf(endCommit);
            if (end == -1) {
                log.error("cannot find end commit {}", endCommit);
            } else {
                toScanCommitList = toScanCommitList.subList(0, end + 1);
                log.info("setting end commit {}", endCommit);
            }
        }

        // 从未扫描的commit列表中  选择特定的commit扫描
        toScanCommitList = filterToScanCommitList(repoUuid, toScanCommitList);

        if (toScanCommitList.isEmpty()) {
            log.warn("repoUUid [{}] : toScanCommitList is null", repoUuid);
            return null;
        }

        log.info("commit size : {}", toScanCommitList.size());

        RepoScan repoScan = getRepoScan(repoUuid, tool, branch);
        //fixme 非首次查询返回repoScan不为null repoScan中totalCommitCount设为0
        if (repoScan == null || repoScan.getTotalCommitCount() == 0) {
            repoScan = RepoScan.builder()
                    .repoUuid(repoUuid)
                    .branch(branch)
                    .scanStatus(OnceScanInfo.SCANNING)
                    .initialScan(true)
                    .tool(tool)
                    .scannedCommitCount(0)
                    .startScanTime(LocalDateTime.now())
                    .endScanTime(LocalDateTime.now())
                    .totalCommitCount(toScanCommitList.size())
                    .scanTime(0)
                    .startCommit(toScanCommitList.get(0))
                    .endCommit(toScanCommitList.get(toScanCommitList.size() - 1))
                    .build();
            insertRepoScan(repoScan);
        } else {
            if (OnceScanInfo.FAILED.equals(repoScan.getScanStatus())) {
                log.error("please check the repo last time why failed!");
                throw new RepoScanGetException("the repo scan last time failed");
            } else {
                repoScan.setTotalCommitCount(repoScan.getScannedCommitCount() + toScanCommitList.size());
            }
            updateRepoScan(repoScan);
        }
        // loadData 用于传输扫描的信息
        specificTool.loadData(repoUuid, branch, repoPath, initialScan, toScanCommitList, repoScan, repoScan.getScannedCommitCount());

        return repoScan;
    }

    /**
     * 停止对某个代码库的扫描
     * @param repoUuid repoUuid
     * @param toolName 工具名
     * @return 是否已经停止
     */
    public boolean stopScan(String repoUuid, String toolName) {
        Assert.notNull(repoUuid, "repoUuid is null");
        Assert.notNull(toolName, "toolName is null");

        String threadName = generateKey(repoUuid, toolName);
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int activeCount = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[activeCount];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < activeCount; i++) {
            if (threadName.equals(lstThreads[i].getName())) {
                lstThreads[i].interrupt();
                return true;
            }
        }

        return false;
    }

    /**
     * 停止对某个代码库的扫描
     * @param repoUuid repoUuid
     * @return 是否已经停止
     */
    public boolean stopScan(String repoUuid) {
        Assert.notNull(repoUuid, "repoUuid is null");

        int keyCount;
        Set<String> targetKeys;
        synchronized (LOCK) {
            Set<String> keys = scanStatusMap.keySet();
            targetKeys = keys.stream().filter(key -> key.contains(repoUuid)).collect(Collectors.toSet());
            keyCount = targetKeys.size();
        }

        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int activeCount = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[activeCount];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < activeCount; i++) {
            if (targetKeys.contains(lstThreads[i].getName())) {
                lstThreads[i].interrupt();
                keyCount--;
            }
        }

        return keyCount == 0;
    }

    /**
     * 根据特定的项目 或者 服务 筛选出需要扫描的commit列表
     * @param repoUuid repoUuid
     * @param toScanCommitList 需要筛选的commit列表
     * @return 过滤后的commit 列表
     **/
    protected List<String> filterToScanCommitList(String repoUuid, List<String> toScanCommitList) {
        return toScanCommitList;
    }

    /**
     * 删除代码库所有数据
     * @param repoUuid repoUuid
     */
    @Deprecated
    protected void deleteRepo(String repoUuid) {}

    /**
     * 删除代码库所有数据
     * @param repoUuid repoUuid
     * @param toolName 工具名
     */
    @Deprecated
    protected void deleteRepo(String repoUuid, String toolName) {}


    /**
     * 获取指定工具的ToolScan实现
     *
     * @param tool tool
     * @return toolScanImpl
     */
    protected abstract AbstractToolScan getToolScan(String tool);

    /**
     * 根据repoUuid tool 代码库的地址决定需要调用的工具列表
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return commitList
     */
    protected abstract List<String> getScannedCommitList(String repoUuid, String tool);

    /**
     * 根据表中的记录得到最新扫描的commit id
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return commit
     */
    protected abstract String getLastedScannedCommit(String repoUuid, String tool);

    /**
     * 根据uuid和代码库的地址决定需要调用的工具列表
     *
     * @param repoUuid repoUuid
     * @return toolName
     */
    protected abstract String[] getToolsByRepo(String repoUuid);


    /**
     * 插入当前repo的扫描信息
     *
     * @param repoScan repoScan
     */
    protected abstract void insertRepoScan(RepoScan repoScan);

    /**
     * 获取已扫描的的repo信息
     *
     * @param repoUuid uuid
     * @param tool     tool
     * @param branch   branch
     * @return RepoScan
     */
    protected abstract RepoScan getRepoScan(String repoUuid, String tool, String branch);

    /**
     * 更新扫描信息
     *
     * @param repoScan repo的扫描信息
     */
    protected abstract void updateRepoScan(RepoScan repoScan);

}
