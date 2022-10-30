package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.domain.dbo.IssueScan;
import cn.edu.fudan.violation.mapper.IssueScanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author beethoven
 */
@Repository
public class IssueScanDao {

    protected IssueScanMapper issueScanMapper;

    @Autowired
    public void setScanMapper(IssueScanMapper issueScanMapper) {
        this.issueScanMapper = issueScanMapper;
    }

    public void insertOneIssueScan(IssueScan scan) {
        issueScanMapper.insertOneScan(scan);
    }

    public void deleteIssueScanByRepoIdAndTool(String repoId, String tool) {
        issueScanMapper.deleteIssueScanByRepoIdAndTool(repoId, tool);
    }

    public List<IssueScan> getIssueScanByRepoIdAndStatusAndTool(String repoId, List<String> status, String tool) {
        return issueScanMapper.getIssueScanByRepoIdAndStatusAndTool(repoId, status, tool);
    }

    public IssueScan getLatestIssueScanByRepoIdAndTool(String repoId, String tool) {
        return issueScanMapper.getLatestIssueScanByRepoIdAndTool(repoId, tool);
    }

    public Set<String> getScannedCommitList(String repoUuid, String tool) {
        return new HashSet<>(issueScanMapper.getScannedCommitList(repoUuid, tool));
    }

    public Map<String, String> getScanStatusInRepo(String repoUuid) {
        return issueScanMapper.getScanStatusInRepo(repoUuid);
    }

    public Map<String, String> getScanFailedCommitList(String repoUuid) {
        return issueScanMapper.getScanFailedCommitList(repoUuid);
    }
}
