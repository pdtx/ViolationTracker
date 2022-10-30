package cn.edu.fudan.violation.core.process;

import cn.edu.fudan.violation.domain.dbo.Location;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.dto.LocationMatchResult;
import cn.edu.fudan.violation.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.violation.domain.enums.RawIssueStatus;
import cn.edu.fudan.violation.util.CosineUtil;
import cn.edu.fudan.violation.util.MapSortUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fancying
 */
@Slf4j
public class RawIssueMatcher {

    private static final double SIMILARITY_LOWER_LIMIT = 0.85;
    private static final double SIMILARITY_LOCATION_LIMIT = 0.65;
    private static final Double MATCH_DEGREE_LIMIT = 0.4;
    private static final Double MODERATE_MATCH_DEGREE = 0.5;

    /**
     * fixme raw issue 匹配上之后 如果不是在同一个方法中 还需要考虑 是否存在含有相同名字的方法
     * 匹配两个列表中的 RawIssue
     *
     * @param preRawIssues  pre file 中
     * @param curRawIssues  cur file 中
     * @param curParentName cur file 中所有的 field name 和 method signature
     */
    public static void match(List<RawIssue> preRawIssues, List<RawIssue> curRawIssues, Set<String> curParentName) {
        // 根据type分类 key {type}
        Map<String, List<RawIssue>> typePreRawIssues = preRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getType));
        Map<String, List<RawIssue>> typeCurRawIssues = curRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getType));
        Set<String> curIssueTypes = typeCurRawIssues.keySet();
        for (Map.Entry<String, List<RawIssue>> preEntry : typePreRawIssues.entrySet()) {
            if (!curIssueTypes.contains(preEntry.getKey())) {
                continue;
            }
            List<RawIssue> preRawIssuesT1 = preEntry.getValue();
            List<RawIssue> curRawIssuesT2 = typeCurRawIssues.get(preEntry.getKey());
            // 根据 type 进行循环匹配
            preRawIssuesT1.forEach(r1 -> curRawIssuesT2.forEach(r2 -> match(r1, r2, curParentName)));
            // todo 优化：不存在重复匹配的情况不需要下面的步骤
            //  匹配完成后找到最佳的匹配 key  RawIssueMatchPair    value matchScore
            Map<RawIssueMatchPair, Double> mappedRawIssue = new LinkedHashMap<>(preRawIssuesT1.size() << 1);
            for (RawIssue preRawIssue1 : preRawIssuesT1) {
                for (RawIssueMatchResult r : preRawIssue1.getRawIssueMatchResults()) {
                    RawIssueMatchPair key = new RawIssueMatchPair(preRawIssue1, r.getRawIssue());
                    mappedRawIssue.put(key, r.getMatchingDegree());
                }
            }
            mappedRawIssue = MapSortUtil.sortByValueDesc(mappedRawIssue);
            // 排序 设置 最佳匹配
            for (RawIssueMatchPair map : mappedRawIssue.keySet()) {
                RawIssue preRawIssue = map.getPreRawIssue();
                RawIssue curRawIssue = map.getCurRawIssue();
                if (preRawIssue.getMappedRawIssue() == null && curRawIssue.getMappedRawIssue() == null) {
                    preRawIssue.setMappedRawIssue(curRawIssue);
                    curRawIssue.setMappedRawIssue(preRawIssue);
                    curRawIssue.setIssueId(preRawIssue.getIssueId());
                    preRawIssue.setMatchDegree(mappedRawIssue.get(map));
                    curRawIssue.setMatchDegree(mappedRawIssue.get(map));
                    preRawIssue.setStatus(RawIssueStatus.CHANGED.getType());
                    curRawIssue.setStatus(RawIssueStatus.CHANGED.getType());
                    curRawIssue.setVersion(preRawIssue.getVersion() + 1);
                    curRawIssue.getMatchInfos().add(curRawIssue.generateRawIssueMatchInfo(preRawIssue.getCommitId()));
                }
            }
            // 没匹配上的将mapped设置为false
            preRawIssuesT1.stream().filter(r -> r.getMappedRawIssue() == null).forEach(r -> {
                r.setMapped(false);
                r.setStatus(RawIssueStatus.SOLVED.getType());
            });
            curRawIssuesT2.stream().filter(r -> r.getMappedRawIssue() == null).forEach(r -> {
                r.setMapped(false);
                r.setStatus(RawIssueStatus.ADD.getType());
            });
        }
    }

    /**
     * raw issue 匹配上之后 如果不是在同一个方法中 还需要考虑 是否存在含有相同名字的方法
     * <p>
     * todo 基于不同的缺陷规则 匹配的策略应该有所不同 根据规则抽取核心的代码逻辑匹配
     */
    private static boolean match(RawIssue preRawIssue, RawIssue curRawIssue, Set<String> curParentName) {
        double matchDegree = 0.0;
        List<Location> preLocations = preRawIssue.getLocations();
        List<Location> curLocations = curRawIssue.getLocations();

        int max = Math.max(preLocations.size(), curLocations.size());
        int min = Math.min(preLocations.size(), curLocations.size());
        // locations 的个数不一样 快速判断是不是同一个 raw issue  先设置一半
        // location 的个数必不为0
        if (max >= min + min) {
            return false;
        }

        // 区别测试用例 two issue match one
        double detailScore = preRawIssue.getDetail().equals(curRawIssue.getDetail()) ? 0.1 : 0;

        // 单个location的匹配情况  只有在一个location的情况下 考虑方法重载的情况
        // TODO: 2021/1/5 单个location不在同一个方法内不能匹配上 除非方法是changeSignature （这里应该与追溯方法的匹配阈值一致）
        //  在前面有编译失败的情况下 有两个相似度高的方法情况下可能会匹配不准确
        if (max == 1) {
            Location preLocation = preLocations.get(0);
            Location curLocation = curLocations.get(0);
            String code1 = preLocation.getCode();
            String code2 = curLocation.getCode();

            boolean isSame = (StringUtils.isEmpty(code1) && StringUtils.isEmpty(code2)) || preLocation.isSame(curLocation);
            if (isSame) {
                matchDegree = calculateMatchDegree(preLocation, curLocation, 1.00, curParentName);
                if (MATCH_DEGREE_LIMIT.equals(matchDegree)) {
                    return false;
                }
                preRawIssue.addRawIssueMappedResult(curRawIssue, matchDegree + detailScore);
                curRawIssue.addRawIssueMappedResult(preRawIssue, matchDegree + detailScore);
                return true;
            }

            double tokenSimilarityDegree = CosineUtil.cosineSimilarity(preLocation.getTokens(), curLocation.getTokens());
            if (tokenSimilarityDegree > SIMILARITY_LOWER_LIMIT) {
                matchDegree = calculateMatchDegree(preLocation, curLocation, tokenSimilarityDegree, curParentName);
                if (MATCH_DEGREE_LIMIT.equals(matchDegree)) {
                    return false;
                }
                preRawIssue.addRawIssueMappedResult(curRawIssue, matchDegree + detailScore);
                curRawIssue.addRawIssueMappedResult(preRawIssue, matchDegree + detailScore);
                preLocation.setMappedLocation(curLocation, matchDegree);
                curLocation.setMappedLocation(preLocation, matchDegree);
                return true;
            }
            return false;
        }

        // 多个location的匹配情况
        preLocations.forEach(l1 -> curLocations.forEach(l2 -> matchTwoLocation(l1, l2, curParentName)));

        //  匹配完成后计算 匹配到的location个数
        Set<Location> mappedLocations = new HashSet<>(8);

        int mappedNum = 0;
        double score;
        for (Location location : preLocations) {
            if (location.isMatched()) {
                mappedNum++;
            }
            score = 0.0;
            for (LocationMatchResult r : location.getLocationMatchResults()) {
                mappedLocations.add(r.getLocation());
                score = score < r.getMatchingDegree() ? r.getMatchingDegree() : score;
            }
            matchDegree += score;
        }

        // 必须 75% 的location相同才认为是同一个raw issue
        min = Math.min(mappedNum, mappedLocations.size());
        double overlap = (double) min / max;
        if (overlap >= SIMILARITY_LOCATION_LIMIT) {
            matchDegree = matchDegree / mappedNum;
            preRawIssue.addRawIssueMappedResult(curRawIssue, matchDegree + detailScore);
            curRawIssue.addRawIssueMappedResult(preRawIssue, matchDegree + detailScore);
            return true;
        }

        return false;
    }

    private static double calculateMatchDegree(Location location1, Location location2, double tokenSimilarity, Set<String> curParentName) {
        String methodName1 = location1.getAnchorName();
        String methodName2 = location2.getAnchorName();
        boolean method1Empty = StringUtils.isEmpty(methodName1);

        int minOffset = Math.min(location1.getOffset(), location2.getOffset());
        int maxOffset = Math.max(location1.getOffset(), location2.getOffset());
        double result = 0.7 * tokenSimilarity + (maxOffset == 0 ? 0 : 0.1 * minOffset / maxOffset);
        if (!method1Empty && methodName1.equals(methodName2)) {
            result = 0.2 + result;
        } else if (!method1Empty) {
            // 给予一个较小的值 方法名不相同的情况下 边界值是 0.7 + 0.1 = 0.8
            // fixme 方法名不同的情况下是否能匹配上 待定
            if (curParentName == null) {
                result = MODERATE_MATCH_DEGREE;
            } else if (curParentName.contains(methodName1)) {
                result = MATCH_DEGREE_LIMIT;
            }
        }

        return result;
    }

    public static void matchTwoLocation(Location location1, Location location2, Set<String> curParentName) {
        double tokenSimilarityDegree;
        String code1 = location1.getCode();
        String code2 = location2.getCode();

        boolean isCode1Empty = StringUtils.isEmpty(code1);
        boolean isCode2Empty = StringUtils.isEmpty(code2);

        if (isCode1Empty ^ isCode2Empty) {
            tokenSimilarityDegree = 0;
        } else if (isCode1Empty) {
            tokenSimilarityDegree = 1;
        } else {
            tokenSimilarityDegree = CosineUtil.cosineSimilarity(location1.getTokens(), location2.getTokens());
        }
        if (code1.equals(code2)) {
            tokenSimilarityDegree = 1;
        }
        if (tokenSimilarityDegree >= SIMILARITY_LOWER_LIMIT) {
            double matchDegree = calculateMatchDegree(location1, location2, tokenSimilarityDegree, curParentName);
            location1.setMappedLocation(location2, matchDegree);
            location2.setMappedLocation(location1, matchDegree);
        }
    }

    @Data
    @AllArgsConstructor
    static class RawIssueMatchPair {
        RawIssue preRawIssue;
        RawIssue curRawIssue;
    }
}
