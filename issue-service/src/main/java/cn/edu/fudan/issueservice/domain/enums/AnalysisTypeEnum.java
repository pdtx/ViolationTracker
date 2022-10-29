package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 *
 * @author Jerry Zhang
 * create: 2022-07-13 20:05
 */
@Getter
public enum AnalysisTypeEnum {
    PROJECT("project"), BRANCH("branch"), DEVELOPER("developer"), SINGLE_ISSUE("issue");
    private final String type;
    AnalysisTypeEnum(String type) {
        this.type = type;
    }
}
