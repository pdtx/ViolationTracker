package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * 间隔时间
 *
 * @author Jerry Zhang
 * create: 2022-07-12 16:44
 */
@Getter
public enum Interval {
    MONTH("month", 30), // 自然月
    QUARTER("quarter", MONTH.days * 3),
    YEAR("year", 365), // 自然年
    HALF_YEAR("half_year", YEAR.days / 2),
    TWO_YEAR("two_year", YEAR.days * 2);
    private final String interval;
    private final Integer days;

    Interval(String interval, Integer days) {
        this.interval = interval;
        this.days = days;
    }
}
