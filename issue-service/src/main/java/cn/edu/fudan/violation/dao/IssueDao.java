package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.domain.dbo.Issue;
import cn.edu.fudan.violation.mapper.IssueMapper;
import cn.edu.fudan.violation.mapper.RawIssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author beethoven
 * @author joshua
 */
@Repository
public class IssueDao {

    private IssueMapper issueMapper;
    private RawIssueMapper rawIssueMapper;

    @Autowired
    public void setRawIssueMapper(RawIssueMapper rawIssueMapper) {
        this.rawIssueMapper = rawIssueMapper;
    }

    @Autowired
    public void setIssueMapper(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    public void insertIssueList(List<Issue> list) {
        if (list.isEmpty()) {
            return;
        }
        issueMapper.insertIssueList(list);
    }

    public void deleteIssueByRepoIdAndTool(String repoId, String tool) {
        issueMapper.deleteIssueByRepoUuidAndTool(repoId, tool);
    }

    public void batchUpdateIssue(List<Issue> issues) {
        issues.forEach(issue -> issueMapper.batchUpdateIssue(issue));
    }


    public List<Issue> getIssuesByUuid(List<String> issueIds) {
        if (issueIds == null || issueIds.isEmpty()) {
            return new ArrayList<>();
        }
        return issueMapper.getIssuesByIds(issueIds);
    }

    public List<Issue> getIssuesByUuidAndRepoUuid(List<String> issueIds, String repoUuid) {
        if (issueIds == null || issueIds.isEmpty()) {
            return new ArrayList<>();
        }
        return issueMapper.getIssuesByIdsAndRepo(issueIds, repoUuid);
    }




    public int getRemainingIssueCount(String repoUuid) {
        return issueMapper.getRemainingIssueCount(repoUuid);
    }









    public void updateIssuesForIgnore(List<String> ignoreFiles, String repoUuid) {
        if (ignoreFiles.isEmpty()) {
            return;
        }
        issueMapper.updateIssuesForIgnore(ignoreFiles, repoUuid);
    }



    public Set<String> getSolvedIssueUuidsByRepoUuid(String repoUuid) {
        return issueMapper.getSolvedIssueUuidsByRepoUuid(repoUuid);
    }



    public List<String> getSolvedIssuesByTypesAndFile(String repoUuid, List<String> issueTypes, String file) {
        if (issueTypes == null || issueTypes.isEmpty()) {
            return new ArrayList<>();
        }
        return issueMapper.getSolvedIssuesByTypeAndFile(repoUuid, issueTypes, file);
    }


}
