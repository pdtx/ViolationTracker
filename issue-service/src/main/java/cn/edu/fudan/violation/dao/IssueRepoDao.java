package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.domain.scan.RepoScan;
import cn.edu.fudan.violation.mapper.IssueRepoMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * @author beethoven
 */
@Repository
public class IssueRepoDao {

    private IssueRepoMapper issueRepoMapper;

    public void insertOneIssueRepo(RepoScan issueRepo) {
        issueRepoMapper.insertOneIssueRepo(issueRepo);
    }

//    public void insertOneIssueRepo(RepoScan issueRepo) {
//        issueRepoMapper.insertOneIssueRepo(issueRepo, UUID.randomUUID().toString());
//    }

    public void updateIssueRepo(RepoScan issueRepo) {
        issueRepoMapper.updateIssueRepo(issueRepo);
    }

    public void delIssueRepo(String repoId, String tool) {
        issueRepoMapper.deleteIssueRepoByCondition(repoId, tool);
    }

    public List<RepoScan> getIssueRepoByCondition(String repoId, String tool) {
        return issueRepoMapper.getIssueRepoByCondition(repoId, tool);
    }

    public List<HashMap<String, Integer>> getNotScanCommitsCount(String repoUuid, String tool) {
        return issueRepoMapper.getNotScanCommitsCount(repoUuid, tool);
    }

    public RepoScan getMainIssueRepo(String repoUuid, String tool) {
        return issueRepoMapper.getMainIssueRepo(repoUuid, tool);
    }

    public RepoScan getRepoScan(String repoUuid, String tool) {
        return issueRepoMapper.getRepoScan(repoUuid, tool);
    }

    public String getStartCommitTime(String repoUuid, String tool) {
        return issueRepoMapper.getStartCommitTime(repoUuid, tool);
    }

    public String getStartCommit(@Param("repoUuid") String repoUuid, @Param("tool") String tool){
        return issueRepoMapper.getStartCommit(repoUuid, tool);
    }

    public List<String> getStartCommits(@Param("repoUuids") List<String> repoUuids, @Param("tool") String tool){
        return issueRepoMapper.getStartCommits(repoUuids, tool);
    }


    @Autowired
    public void setIssueRepoMapper(IssueRepoMapper issueRepoMapper) {
        this.issueRepoMapper = issueRepoMapper;
    }
}
