package cn.edu.fudan.violation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
@AllArgsConstructor
public enum RepoStatusEnum {

    /**
     * repo scan status
     */
    SCANNING("scanning"),
    STOP("stop"),
    FAILED("failed"),
    COMPLETE("complete");

    private final String type;
}
