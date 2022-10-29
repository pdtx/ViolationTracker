package cn.edu.fudan.issueservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * description: save repo info
 *
 * @author fancying
 * create: 2020-04-24 10:38
 **/
@Data
@Builder
@AllArgsConstructor
public class RepoResourceDTO {
    String repoUuid;
    String repoPath;

    public RepoResourceDTO() {
        repoUuid = "";
        repoPath = "";
    }

}