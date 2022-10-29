package cn.edu.fudan.issueservice.scan;

import cn.edu.fudan.issueservice.domain.scan.RepoScan;
import cn.edu.fudan.issueservice.domain.scan.ScanData;

import java.util.List;


/**
 * 不同工具扫描流程
 *
 * @author fancying
 * @author beethoven
 */
public abstract class AbstractToolScan {

    protected ScanData scanData = new ScanData();

    void loadData(String repoUuid, String branch, String repoPath, boolean initialScan, List<String> toScanCommitList, RepoScan repoScan, Integer scannedCommitCount) {
        if (scanData == null) {
            scanData = new ScanData();
        }
        scanData.setBranch(branch);
        scanData.setRepoPath(repoPath);
        scanData.setRepoUuid(repoUuid);
        scanData.setToScanCommitList(toScanCommitList);
        scanData.setInitialScan(initialScan);
        scanData.setRepoScan(repoScan);
        scanData.setScannedCommitCount(scannedCommitCount);

        // 将代理对象的scanData 数据也设置
        setScanData(scanData);
    }

    public ScanData getScanData() {
        return scanData;
    }

    public void setScanData(ScanData scanData) {
         this.scanData = scanData;
    }

    /**
     * 由子类实现具体扫描流程
     *
     * @param commit commit
     * @return 是否扫描成功
     *
     * @throws Exception e
     */
    public abstract boolean scanOneCommit(String commit) throws Exception;

    /**
     * 开始扫描commit列表之前的准备工作,可以为空方法
     *
     * @throws Exception e
     **/
    public void prepareForScan() throws Exception {}

    /**
     * 开始扫描一个commit之前的准备工作,可以为空方法
     *
     * @param commit commit
     */
    public void prepareForOneScan(String commit) {}

    /**
     * 完成扫描一个commit之后的清理工作,可以为空方法
     *
     * @param commit commit
     */
    public void cleanUpForOneScan(String commit) {}

    /**
     * 完成扫描commit列表之后的清理工作,可以为空方法
     **/
    public void cleanUpForScan() {}

}
