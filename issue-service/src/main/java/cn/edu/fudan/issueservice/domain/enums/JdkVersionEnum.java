package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * @author beethoven
 * @date 2021-09-15 10:38:58
 */
@Getter
public enum JdkVersionEnum {
    /**
     * jdk版本
     */
    JDK_8("1.8"),
    JDK_11("11"),
    JDK_12("12"),
    JDK_16("16");

    private final String version;

    JdkVersionEnum(String version) {
        this.version = version;
    }

}
