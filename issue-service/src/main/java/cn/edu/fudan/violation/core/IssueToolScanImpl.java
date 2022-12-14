package cn.edu.fudan.violation.core;

import cn.edu.fudan.violation.component.RestInterfaceManager;
import cn.edu.fudan.violation.core.analyzer.AnalyzerFactory;
import cn.edu.fudan.violation.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.violation.core.process.IssueMatcher;
import cn.edu.fudan.violation.core.process.IssuePersistenceManager;
import cn.edu.fudan.violation.core.process.IssueStatistics;
import cn.edu.fudan.violation.core.solved.IssueSolved;
import cn.edu.fudan.violation.dao.IssueAnalyzerDao;
import cn.edu.fudan.violation.dao.IssueScanDao;
import cn.edu.fudan.violation.dao.RawIssueMatchInfoDao;
import cn.edu.fudan.violation.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.violation.domain.dbo.IssueScan;
import cn.edu.fudan.violation.domain.enums.ScanStatusEnum;
import cn.edu.fudan.violation.domain.enums.ToolEnum;
import cn.edu.fudan.violation.mapper.SolvedRecordMapper;
import cn.edu.fudan.violation.scan.AbstractToolScan;
import cn.edu.fudan.violation.util.DateTimeUtil;
import cn.edu.fudan.violation.util.FileUtil;
import cn.edu.fudan.violation.util.JGitHelper;
import cn.edu.fudan.violation.util.RawIssueParseUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author beethoven
 * @author fancying
 * @date 2021-04-25 16:56:35
 */
@Component
@Scope("prototype")
@Slf4j
public class IssueToolScanImpl extends AbstractToolScan {

    /**
     * key for commitId
     * value for status
     */
    private final Map<String, ScanStatusEnum> resourcesStatus = new ConcurrentHashMap<>(16);
    private final Lock lock = new ReentrantLock();
    private final Lock lock1 = new ReentrantLock();
    private final Condition hasResource = lock.newCondition();
    private final Condition resourceReady = lock.newCondition();
    @Autowired
    RawIssueMatchInfoDao rawIssueMatchInfoDao;
    @Autowired
    SolvedRecordMapper solvedRecordMapper;
    private IssueScanDao issueScanDao;
    private IssueAnalyzerDao issueAnalyzerDao;

    private IssueSolved issueSolved;
    private AnalyzerFactory analyzerFactory;
    private IssueMatcher issueMatcher;
    private IssueStatistics issueStatistics;
    private IssuePersistenceManager issuePersistenceManager;
    @Value("${scanThreadNum}")
    private int initValue;
    private RestInterfaceManager rest;
    @Value("${enable.target.repo.path}")
    private boolean enableTargetRepoPath;

    @Async("prepare-resource")
    @Override
    public void prepareForScan() throws IOException {

        String repoUuid = scanData.getRepoUuid();
        BaseAnalyzer analyzer = analyzerFactory.createAnalyzer(scanData.getRepoScan().getTool());

        // ??????????????????????????????????????? ?????????????????????????????????
        if (analyzer.closeResourceLoader()) {
            return;
        }

        Thread.currentThread().setName("pr-" + (repoUuid.length() > 7 ? repoUuid.substring(0, 6) : repoUuid));
        log.info("prepare-resource {}", repoUuid);

        AtomicInteger remainingNum = new AtomicInteger(initValue);

        // key repoPath  value valid
        Map<JGitHelper, Boolean> repoResource = new ConcurrentHashMap<>(initValue << 1);

        for (int i = 0; i < initValue; i++) {
            String repoPath;
            if(enableTargetRepoPath){
                FileUtil.copyFile(rest.getCodeServiceRepo(repoUuid), rest.getCodeServiceRepo(repoUuid) + "-" + i);
                repoPath = rest.getCodeServiceRepo(repoUuid) + "-" + i;
            }else {
                repoPath = rest.getCodeServiceRepo(repoUuid);
            }
            JGitHelper jGitHelper = new JGitHelper(repoPath);
            repoResource.put(jGitHelper, true);
        }

        List<String> commitList = scanData.getToScanCommitList();
        String toolName = analyzer.getToolName();
        CountDownLatch completeFlag = new CountDownLatch(commitList.size());

        lock.lock();
        try {
            for (String commit : commitList) {
                // ???????????????????????????????????????
                if (issueAnalyzerDao.cached(repoUuid, commit, toolName)) {
                    log.info("cached {}", commit);
                    completeFlag.countDown();
                    resourcesStatus.put(commit, issueAnalyzerDao.getInvokeResult(repoUuid, commit, toolName) == 1 ? ScanStatusEnum.DONE : ScanStatusEnum.CACHE_FAILED);
                    continue;
                }

                //todo pr ??????
                while (remainingNum.get() == 0) {
                    hasResource.await();
                }
                remainingNum.decrementAndGet();
                resourcesStatus.put(commit, ScanStatusEnum.DOING);
                // ?????????????????????????????? ToolAnalyzer ?????? ???????????????????????? resultRawIssues ???
                WeakReference<BaseAnalyzer> weakSpecificAnalyzer = new WeakReference<>(analyzerFactory.createAnalyzer(scanData.getRepoScan().getTool()));
                Objects.requireNonNull(weakSpecificAnalyzer.get()).
                        produceResource(repoUuid, repoResource, commit, resourcesStatus, remainingNum, completeFlag,
                                lock, hasResource, resourceReady, issueAnalyzerDao, commitList, lock1);
            }
            log.info("begin wait all resource done repoUuid:{}", repoUuid);

            // ?????????????????? ?????????????????? ????????????????????????
            lock.unlock();

            // ?????? produce ??????????????????
            completeFlag.await();

            lock.lock();
            hasResource.signalAll();
            resourceReady.signalAll();
            lock.unlock();

        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            if (Thread.holdsLock(lock)) {
                lock.unlock();
            }
            for (JGitHelper jgit : repoResource.keySet()) {
                rest.freeRepoPath(repoUuid, jgit.getRepoPath());
            }
        }
    }

    @Override
    public void prepareForOneScan(String commit) {
        BaseAnalyzer analyzer = analyzerFactory.createAnalyzer(scanData.getRepoScan().getTool());
        // ??????????????????????????????????????? ?????????????????????????????????
        if (analyzer.closeResourceLoader()) {
            return;
        }
        lock.lock();
        try {
            // ?????? prepare ??????????????????
            while (!resourcesStatus.containsKey(commit) ||
                    resourcesStatus.get(commit).equals(ScanStatusEnum.DOING)) {
                log.info("begin wait resource commit:{}", commit);

                //todo tscancode waiting ??????????????????????????????
                resourceReady.await();

                log.info("end wait resource commit:{}", commit);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean scanOneCommit(String commit) {

        log.info("start scan  commit id --> {}", commit);

        try {
            JGitHelper jGitHelper = new JGitHelper(scanData.getRepoPath());
            BaseAnalyzer analyzer = analyzerFactory.createAnalyzer(scanData.getRepoScan().getTool());

            //1 init IssueScan
            Date commitTime = jGitHelper.getCommitDateTime(commit);
            IssueScan issueScan = IssueScan.initIssueScan(scanData.getRepoUuid(), commit, scanData.getRepoScan().getTool(), commitTime);
            IssueAnalyzer issueAnalyzer = IssueAnalyzer.initIssueAnalyze(scanData.getRepoUuid(), commit, scanData.getRepoScan().getTool());

            //2 checkout
            jGitHelper.checkout(commit);

            //3 execute scan
            scan(issueAnalyzer, issueScan, scanData.getRepoPath(), analyzer, jGitHelper);

            //4 update issue scan end time and persistence
            issueScan.setEndTime(new Date());
            boolean scanPersistenceResult = afterOneCommitScanPersist(issueScan, issueAnalyzer);

            String scanPersistenceStatus = "success";
            if (!scanPersistenceResult) {
                scanPersistenceStatus = "failed";
            }
            log.info("issue scan result  persist {}! commit id --> {}", scanPersistenceStatus, commit);

            analyzer.emptyAnalyzeRawIssues();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean afterOneCommitScanPersist(IssueScan issueScan, IssueAnalyzer issueAnalyzer) {
        try {
            issueScanDao.insertOneIssueScan(issueScan);
            issueAnalyzerDao.insertIssueAnalyzer(issueAnalyzer);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void scan(IssueAnalyzer issueAnalyzer, IssueScan issueScan, String repoPath, BaseAnalyzer analyzer, JGitHelper jGitHelper) throws InterruptedException {

        String repoUuid = issueScan.getRepoUuid();
        String commit = issueScan.getCommitId();

        JSONObject analyzeCache = issueAnalyzerDao.getAnalyzeResultByRepoUuidCommitIdTool(repoUuid, commit, analyzer.getToolName());
        boolean cached = issueAnalyzerDao.cached(repoUuid, commit, analyzer.getToolName());
//        boolean prepareAdvance = resourcesStatus.containsKey(commit);
        boolean prepareSuccess = resourceLoadStatus(repoUuid, repoPath, commit, issueScan, analyzer, issueAnalyzer, jGitHelper);

        //0 analyze before

        // ??????????????????
        // ??????????????? a ????????????????????????????????????(????????????)??????  ??????  b ???????????????????????????????????????
        boolean skip = (!cached && !prepareSuccess) || (cached && analyzeCache == null);

        // c ????????????????????????????????????
        if (analyzeCache != null) {
            log.info("analyze raw issues in this commit:{} before, go ahead to mapping issue!", commit);
            analyzer.setResultRawIssues(RawIssueParseUtil.json2RawIssues(analyzeCache));
        } else if (skip) {
            return;
        }
        log.info("analyze raw issues success!");

        // d ????????????????????????????????????
        issueAnalyzer.updateIssueAnalyzeStatus(analyzer.getResultRawIssues());

        //4 issue match
        long matchStartTime = System.currentTimeMillis();
        issueMatcher.setAnalyzer(analyzer);
        boolean matchResult = issueMatcher.matchProcess(repoUuid, commit, jGitHelper, analyzer.getToolName(), analyzer.getResultRawIssues());
        if (!matchResult) {
            log.error("issue match failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.MATCH_FAILED.getType());
            return;
        }
        long matchTime = System.currentTimeMillis();
        log.info("issue match use {} s,match success!", (matchTime - matchStartTime) / 1000);

        //5 issue statistics
        initIssueStatistics(commit, analyzer, jGitHelper);
        boolean statisticalResult = issueStatistics.doingStatisticalAnalysis(issueMatcher, repoUuid, analyzer.getToolName());
        if (!statisticalResult) {
            log.error("statistical failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.STATISTICAL_FAILED.getType());
            return;
        }
        long issueStatisticsTime = System.currentTimeMillis();
        log.info("issue statistics use {} s,issue statistics success!", (issueStatisticsTime - matchTime) / 1000);

        //6 issue merge
//        long issueMergeTime = System.currentTimeMillis();
//        // ???????????????commit???????????????issue???????????????
//        if(issueAnalyzerDao.getIsTotalScan(repoUuid, commit, issueAnalyzer.getTool()) == 1){
//            boolean mergeResult = issueMergeManager.issueMerge(issueStatistics, repoUuid);
//            if (!mergeResult) {
//                log.error("merge failed!repo path is {}, commit is {}", repoPath, commit);
//                return;
//            }
//        }
//        log.info("issue merge use {} s,issue merged!", (System.currentTimeMillis() - issueMergeTime) / 1000);

        //7 issue persistence
        try {
            issuePersistenceManager.persistScanData(issueStatistics, repoUuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("persist failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.STATISTICAL_FAILED.getType());
            return;
        }
        log.info("issue persistence use {} s,issue persistence!", (System.currentTimeMillis() - issueStatisticsTime) / 1000);

        issueScan.setStatus(ScanStatusEnum.DONE.getType());
    }

    /**
     * ???????????? ?????? rawIssue
     */
    private boolean resourceLoadStatus(String repoUuid, String repoPath, String commit,
                                       IssueScan issueScan, BaseAnalyzer toolAnalyzer, IssueAnalyzer issueAnalyzer, JGitHelper jGitHelper) {
        if (!resourcesStatus.containsKey(commit)) {
            ScanStatusEnum scanStatusEnum = ScanStatusEnum.CHECKOUT_FAILED;
            if (jGitHelper.checkout(commit)) {
                // ?????????parent?????????????????????????????????????????????????????????
                try {
                    String scanRepoPath = toolAnalyzer.getScanRepoPath(repoUuid, commit, jGitHelper, issueAnalyzerDao, scanData.getToScanCommitList());
                    if (scanRepoPath != null) {
                        repoPath = scanRepoPath;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                scanStatusEnum = toolAnalyzer.invokeAndAnalyze(repoPath, repoUuid, commit, jGitHelper);
                issueScan.setStatus(scanStatusEnum.getType());
                if (scanStatusEnum.equals(ScanStatusEnum.DONE)) {
                    issueAnalyzer.updateIssueAnalyzeStatus(toolAnalyzer.getResultRawIssues());
                    issueAnalyzer.setIsTotalScan(toolAnalyzer.isTotalScan()?1:0);
                    issueAnalyzerDao.insertIssueAnalyzer(issueAnalyzer);
                } else {
                    issueAnalyzer.setInvokeResult(IssueAnalyzer.InvokeResult.FAILED.getStatus());
                    return false;
                }
            } else {
                issueAnalyzer.setInvokeResult(IssueAnalyzer.InvokeResult.FAILED.getStatus());
                issueScan.setStatus(scanStatusEnum.getType());
                log.error("checkout failed repo Path is {}", jGitHelper.getRepoPath());
                return false;
            }
        } else {
            ScanStatusEnum scanStatusEnum = resourcesStatus.get(commit);
            issueScan.setStatus(scanStatusEnum.getType());
            resourcesStatus.remove(commit);
            if (!scanStatusEnum.equals(ScanStatusEnum.DONE)) {
                return false;
            }
            //????????????????????? ???????????????????????????
            issueAnalyzer.updateIssueAnalyzeStatus(toolAnalyzer.getResultRawIssues());
        }
        return true;
    }

    private void initIssueStatistics(String commit, BaseAnalyzer analyzer, JGitHelper jGitHelper) {
        issueStatistics.setCommitId(commit);
        issueStatistics.setCurrentCommitDate(DateTimeUtil.localToUtc(jGitHelper.getCommitTime(commit)));
        issueStatistics.setJGitHelper(jGitHelper);
    }

    @Override
    public void cleanUpForOneScan(String commit) {
        issueMatcher.cleanParentRawIssueResult();
        issueStatistics.cleanRawIssueUuid2DataBaseUuid();
    }

    @Override
    public void cleanUpForScan() {

        if (!ToolEnum.SONAR.getType().equals(scanData.getRepoScan().getTool())) {
            return;
        }

        // ?????? solved ??????
        String repoUuid = scanData.getRepoUuid();
        String repoPath = scanData.getRepoPath();
        boolean needNotNullSolveWay = scanData.isInitialScan();
        // ??????????????????????????? solve way
        // ??????????????? ?????????solved way ??? null ?????????
        issueSolved.updateSolvedWay(repoUuid, repoPath, true);

        log.info("judgeSolvedType repo {} done", repoUuid);
    }

    @Autowired
    public void setIssueSolved(IssueSolved issueSolved) {
        this.issueSolved = issueSolved;
    }

    @Autowired
    public void setAnalyzerFactory(AnalyzerFactory analyzerFactory) {
        this.analyzerFactory = analyzerFactory;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }


    @Autowired
    public void setIssueMatcher(IssueMatcher issueMatcher) {
        this.issueMatcher = issueMatcher;
    }

    @Autowired
    public void setIssueStatistics(IssueStatistics issueStatistics) {
        this.issueStatistics = issueStatistics;
    }

    @Autowired
    public void setIssuePersistenceManager(IssuePersistenceManager issuePersistenceManager) {
        this.issuePersistenceManager = issuePersistenceManager;
    }

    @Autowired
    public void setIssueAnalyzerDao(IssueAnalyzerDao issueAnalyzerDao) {
        this.issueAnalyzerDao = issueAnalyzerDao;
    }

    @Autowired
    public void setRest(RestInterfaceManager rest) {
        this.rest = rest;
    }

}
