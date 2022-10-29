package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * issue整体分析 or 趋势分析的分析方面
 * @author Jerry Zhang
 * create: 2022-07-13 16:40
 */
@Getter
public enum AnalysisFacetEnum {
    /**
     * 不同的维度缺陷数据
     */
    OVERALL("overall"),
    CATEGORY("category"),
    SEVERITY("severity"),
    TYPE("type");
    private final String facet;
    AnalysisFacetEnum(String facet) {
        this.facet = facet;
    }
}
