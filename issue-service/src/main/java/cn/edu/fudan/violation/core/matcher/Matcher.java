package cn.edu.fudan.violation.core.matcher;

import cn.edu.fudan.violation.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.violation.core.process.RawIssueMatcher;
import cn.edu.fudan.violation.core.service.CoreService;
import cn.edu.fudan.violation.dao.IssueDao;
import cn.edu.fudan.violation.dao.IssueTypeDao;
import cn.edu.fudan.violation.dao.RawIssueDao;
import cn.edu.fudan.violation.dao.RawIssueMatchInfoDao;
import cn.edu.fudan.violation.domain.dbo.Issue;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.dto.MatcherResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author beethoven
 * @date 2021-09-22 14:18:44
 */
public interface Matcher {

    /**
     * 匹配两个 raw issue list
     *
     * @param preRawIssues preRawIssues
     * @param curRawIssues curRawIssues
     * @param analyzer     analyzer
     * @param repoPath     repoPath
     */
    default void mapRawIssues(List<RawIssue> preRawIssues, List<RawIssue> curRawIssues, BaseAnalyzer analyzer, String repoPath) {
        Map<String, List<RawIssue>> preRawIssueMap = preRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));
        Map<String, List<RawIssue>> curRawIssueMap = curRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));

        preRawIssueMap.entrySet().stream()
                .filter(e -> curRawIssueMap.containsKey(e.getKey()))
                .forEach(pre -> {
                    Set<String> methodsAndFields = Collections.emptySet();
                    try {
                        methodsAndFields = analyzer.getMethodsAndFieldsInFile(repoPath + "/" + pre.getKey());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    RawIssueMatcher.match(pre.getValue(), curRawIssueMap.get(pre.getKey()), methodsAndFields);
                });
    }

    /**
     * 为匹配文件重命名预处理
     *
     * @param preRawIssues preRawIssues
     * @param map          map
     */
    default void renameHandle(List<RawIssue> preRawIssues, Map<String, String> map) {
        preRawIssues.stream()
                .filter(r -> map.containsKey(r.getFileName()))
                .forEach(rawIssue -> {
                    rawIssue.getLocations().forEach(location -> location.setFilePath(map.get(rawIssue.getFileName())));
                    rawIssue.setFileName(map.get(rawIssue.getFileName()));
                });
    }

    /**
     * 匹配 current commit raw issues 和 parent commit raw issues
     *
     * @return matcher result
     */
    MatcherResult matchRawIssues();

    /**
     * 统计 raw issues 匹配情况
     *
     * @param parentCommit parentCommit
     * @param preRawIssues preRawIssues
     * @param curRawIssues curRawIssues
     * @param oldIssuesMap oldIssuesMap
     */
    void sumUpRawIssues(String parentCommit, List<RawIssue> preRawIssues, List<RawIssue> curRawIssues, Map<String, Issue> oldIssuesMap);

    /**
     * 初始化 dao
     *
     * @param logHome              logHome
     * @param issueDao             issueDao
     * @param issueTypeDao         issueTypeDao
     * @Param rawIssueDao          rawIssueDao
     * @param rawIssueMatchInfoDao rawIssueMatchInfoDao
     * @param coreService          coreService
     */
    void init(String logHome, IssueDao issueDao, IssueTypeDao issueTypeDao, RawIssueDao rawIssueDao, RawIssueMatchInfoDao rawIssueMatchInfoDao, CoreService coreService);
}
