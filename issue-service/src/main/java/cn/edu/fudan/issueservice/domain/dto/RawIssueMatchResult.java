package cn.edu.fudan.issueservice.domain.dto;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * todo matchedIssueId matchedRawIssueId  可以删除 被 rawIssue 和 issue 取代
 *
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawIssueMatchResult {

    /**
     * 匹配到的IssueId
     */
    String matchedIssueId;

    /**
     * 匹配到的RawIssue Id
     */
    String matchedRawIssueId;

    /**
     * 两个raw issue 是否完全一样
     */
    boolean isBestMatch = false;

    /**
     * 两个rawIssue的匹配度
     */
    double matchingDegree;

    /**
     * 匹配到的RawIssue
     */
    RawIssue rawIssue;

    public static RawIssueMatchResult newInstance(RawIssue rawIssue, double matchDegree) {
        RawIssueMatchResult result = new RawIssueMatchResult();
        result.setMatchedIssueId(rawIssue.getIssueId());
        result.setRawIssue(rawIssue);
        result.setMatchedRawIssueId(rawIssue.getUuid());
        result.setMatchingDegree(matchDegree);
        return result;
    }
}
