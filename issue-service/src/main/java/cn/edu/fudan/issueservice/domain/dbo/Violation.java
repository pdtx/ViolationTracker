package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dto.AnalysisIssue;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Jerry Zhang <https://github.com/doughit>
 * @Description
 * @Copyright DoughIt Studio - Powered By DoughIt
 * @date 2022-08-03 09:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Violation {
    private String repoName;
    private String repoUuid;
    private String issueUuid;
    private String type;
    private String startCommit;
    private String endCommit;
    private String startCommitTime;
    private String endCommitTime;
    private String startRawIssueUuid;
    private String endRawIssueUuid;
    private String preFile;
    private String curFile;
    private List<Integer> lines;
    private List<String> tokens;
    // 修复天数
    private Integer lifeCycle;
    // startCommit -> endCommit 最短路径
    private Integer commitNum;
    // merge solve、solved
    private String status;
    private String solvedWay;
    private Integer reopen;
    private Integer mergeNew;
    private Integer mergeReopen;
    private Integer mergeNormalSolve;
    private Integer mergeDeleteSolve;

    public Violation(String repoUuid, String repoName, AnalysisIssue start, AnalysisIssue end) {
        setRepoName(repoName);
        setRepoUuid(repoUuid);
        setIssueUuid(end.getIssueUuid());
        setEndCommit(end.getCommitId());
        setEndCommitTime(end.getCommitTime());
        setEndRawIssueUuid(end.getRawIssueUuid());
        setStatus(end.getStatus());
        setSolvedWay(end.getSolveWay());
        if (start == null) {
            setStartCommit(null);
            setStartCommitTime(null);
            setStartRawIssueUuid(null);
            setLifeCycle(null);
        } else {
            setStartCommit(start.getCommitId());
            setStartCommitTime(start.getCommitTime());
            setStartRawIssueUuid(start.getRawIssueUuid());
            setLifeCycle(DateTimeUtil.dateDiff(start.getCommitTime(), end.getCommitTime()));
        }
    }
}
