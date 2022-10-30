package cn.edu.fudan.violation.core.matcher;

import cn.edu.fudan.violation.domain.dto.MatcherData;
import cn.edu.fudan.violation.domain.enums.CommitStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author beethoven
 * @date 2021-09-22 14:30:42
 */
@Component
public class MatcherFactory {

    public Matcher createMatcher(CommitStatusEnum commitStatus, MatcherData data) {
        if (CommitStatusEnum.FIRST.equals(commitStatus)) {
            return new FirstMatcher(data);
        } else if (CommitStatusEnum.NORMAL.equals(commitStatus)) {
            return new NormalMatcher(data);
        } else {
            return new MergeMatcher(data);
        }
    }

}
