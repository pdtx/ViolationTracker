package cn.edu.fudan.violation.domain.dbo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Beethoven
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgnoreRecord {
    private String uuid;
    private String accountUuid;
    private String accountName;
    private int level;
    private String type;
    private String tool;
    private String repoUuid;
    private String repoName;
    private String branch;
    private String ignoreTime;
    private String rawIssue;
    private String commitUuid;
    private String issueUuid;
    private String tag;
    private String message;
    private String filePath;
    private int isUsed;
}
