package cn.edu.fudan.violation.domain.dbo;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeff
 * @author fancying
 * @author heyue
 * @author PJH
 */
@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueAnalyzer {
    private String repoUuid;
    private String commitId;
    private int invokeResult;
    private JSONObject analyzeResult;
    private String tool;
    private int rawIssueNum;
    //用于分片
    private int sharding;
    //用于判断是否为全量扫描 1:是 0:不是
    private int isTotalScan;
    /**
     * 该字段仅用于传递值，不需要存入数据库中
     */
    @Transient
    private List<RawIssue> rawIssueList;

    public static IssueAnalyzer initIssueAnalyze(String repoUuid, String commitId, String tool) {
        return IssueAnalyzer.builder()
                .repoUuid(repoUuid)
                .commitId(commitId)
                .invokeResult(InvokeResult.FAILED.getStatus())
                .analyzeResult(new JSONObject())
                .rawIssueList(new ArrayList<>())
                .tool(tool)
                .build();
    }

    public void updateIssueAnalyzeStatus(List<RawIssue> resultRawIssues) {
        this.setRawIssueList(resultRawIssues);
        this.setRawIssueNum(resultRawIssues.size());
    }

    @Getter
    public enum InvokeResult {
        /**
         * issue analyzer 状态
         */
        SUCCESS(1),
        FAILED(0);

        private final int status;

        InvokeResult(int status) {
            this.status = status;
        }
    }

}
