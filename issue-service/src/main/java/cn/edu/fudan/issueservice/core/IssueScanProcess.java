package cn.edu.fudan.issueservice.core;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.scan.OnceScanInfo;
import cn.edu.fudan.issueservice.domain.scan.RepoScan;
import cn.edu.fudan.issueservice.scan.AbstractToolScan;
import cn.edu.fudan.issueservice.scan.BaseScanProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author beethoven
 * @author pjh
 * @date 2021-04-25 13:51:11
 */
@Component
public class IssueScanProcess extends BaseScanProcess {

    private IssueScanDao issueScanDao;

    private IssueRepoDao issueRepoDao;

    private RestInterfaceManager restInterfaceManager;

    @Value("${enableAllScan}")
    private boolean enableAllScan;

    private boolean isFirstScan;

    public IssueScanProcess(@Autowired ApplicationContext applicationContext) {
        super(applicationContext);
    }

//
//    public ToolScan getToolScan() {
//        return applicationContext.getBean(ToolScanImpl.class);
//    }

    @Override
    protected AbstractToolScan getToolScan(String tool) {
        return applicationContext.getBean(IssueToolScanImpl.class);
    }

    @Override
    protected List<String> getScannedCommitList(String repoUuid, String tool) {
        return new ArrayList<>(issueScanDao.getScannedCommitList(repoUuid, tool));
    }

    @Override
    protected String getLastedScannedCommit(String repoUuid, String tool) {
        IssueScan latestIssueScan = issueScanDao.getLatestIssueScanByRepoIdAndTool(repoUuid, tool);
        if (latestIssueScan == null) {
            return null;
        }
        return latestIssueScan.getCommitId();
    }

    @Override
    protected String[] getToolsByRepo(String repoUuid) {
        return restInterfaceManager.getToolsByRepoUuid(repoUuid);
    }

    @Override
    protected void insertRepoScan(RepoScan repoScan) {
        issueRepoDao.insertOneIssueRepo(repoScan);
    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {
        issueRepoDao.updateIssueRepo(scanInfo);
    }

    @Override
    public void deleteRepo(String repoUuid) {

    }

    @Override
    protected RepoScan getRepoScan(String repoUuid, String tool, String branch) {

        RepoScan repoScan = issueRepoDao.getRepoScan(repoUuid, tool);
        if (repoScan == null) {
            return null;
        }

        if (OnceScanInfo.FAILED.equals(repoScan.getScanStatus())) {
            return repoScan;
        }

        repoScan.setScanStatus(OnceScanInfo.SCANNING);
        return repoScan;
    }

    @Override
    public void deleteRepo(String repoUuid, String toolName) {

    }

    @Override
    public List<String> filterToScanCommitList(String repoUuid, List<String> toScanCommitList) {
        //如果不是全量扫描，且是第一次扫描，只扫描最后一个
        if (isFirstScan && !enableAllScan && !toScanCommitList.isEmpty()) {
            return Collections.singletonList(toScanCommitList.get(toScanCommitList.size() - 1));
        }
        return toScanCommitList;
    }

    public void setFirstScan(boolean isFirstScan) {
        this.isFirstScan = isFirstScan;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }
}
