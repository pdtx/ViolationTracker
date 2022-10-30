package cn.edu.fudan.violation.core.matcher;

import cn.edu.fudan.violation.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.violation.core.service.CoreService;
import cn.edu.fudan.violation.dao.IssueDao;
import cn.edu.fudan.violation.dao.IssueTypeDao;
import cn.edu.fudan.violation.dao.RawIssueDao;
import cn.edu.fudan.violation.dao.RawIssueMatchInfoDao;
import cn.edu.fudan.violation.domain.dbo.Issue;
import cn.edu.fudan.violation.domain.dbo.IssueType;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.dto.MatcherResult;
import cn.edu.fudan.violation.domain.enums.IssuePriorityEnums;
import cn.edu.fudan.violation.util.JGitHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author beethoven
 * @date 2021-09-22 17:16:26
 */
@Slf4j
public abstract class BaseMatcher implements Matcher {

    protected final String SEPARATOR = System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/";
    protected String logHome;
    protected String tool;
    protected String repoUuid;
    protected String currentCommit;
    protected BaseAnalyzer analyzer;
    protected JGitHelper jGitHelper;
    protected List<RawIssue> currentRawIssues;
    protected Map<String, List<String>> commitFileMap;
    protected Map<String, List<RawIssue>> parentRawIssuesMap;
    protected Map<String, Map<String, String>> preFile2CurFileMap;
    protected Map<String, Map<String, String>> curFile2PreFileMap;
    protected IssueDao issueDao;
    protected IssueTypeDao issueTypeDao;
    protected RawIssueDao rawIssueDao;
    protected RawIssueMatchInfoDao rawIssueMatchInfoDao;
    protected CoreService coreService;
    protected ApplicationContext applicationContext;
    protected MatcherResult matcherResult;

    @Override
    public void init(String logHome, IssueDao issueDao, IssueTypeDao issueTypeDao, RawIssueDao rawIssueDao, RawIssueMatchInfoDao rawIssueMatchInfoDao, CoreService coreService) {
        this.logHome = logHome;
        this.issueDao = issueDao;
        this.issueTypeDao = issueTypeDao;
        this.rawIssueDao = rawIssueDao;
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
        this.coreService = coreService;
    }

    @Override
    public MatcherResult matchRawIssues() {
        if (parentRawIssuesMap.size() == 1) {
            log.info("commit {} start normal match, parent commit is {}", currentCommit, parentRawIssuesMap.keySet());
        } else {
            log.info("commit {} start merge match, parent commit is {}", currentCommit, parentRawIssuesMap.keySet());
        }

        boolean isFirst = true;
        for (Map.Entry<String, List<RawIssue>> entry : parentRawIssuesMap.entrySet()) {
            String parentCommit = entry.getKey();

            // 获取匹配准备所需数据
            List<String> files = commitFileMap.get(parentCommit);
            Map<String, String> preFile2CurFile = preFile2CurFileMap.get(parentCommit);
            Map<String, String> curFile2PreFile = curFile2PreFileMap.get(parentCommit);

            // 获取需要匹配的 raw issues 并标记其他 raw issues 为 not change
            List<RawIssue> curRawIssues = currentRawIssues.stream().filter(r -> files.contains(r.getFileName())).collect(Collectors.toList());
//            currentRawIssues.stream().filter(r -> !curRawIssues.contains(r)).forEach(rawIssue -> rawIssue.setOnceMapped(true));

            // 匹配 raw issues
            List<RawIssue> preRawIssues = parentRawIssuesMap.get(parentCommit).stream().
                    filter(rawIssue -> {
                        String issueId = rawIssue.getIssueId();
                        if (issueId == null) {
                            return false;
                        }
                        return issueDao.getIssuesByUuid(List.of(issueId)) != null;
                    }).collect(Collectors.toList());

            renameHandle(preRawIssues, preFile2CurFile);

            long startMatchTime = System.currentTimeMillis();
            mapRawIssues(preRawIssues, curRawIssues, analyzer, jGitHelper.getRepoPath());
            long endMatchTime = System.currentTimeMillis();
            log.info("commit: {} and pre commit:{} cost {} ms", currentCommit, parentCommit, endMatchTime - startMatchTime);
//            String content = endMatchTime - startMatchTime + " ms  " + currentCommit + " --- " + parentCommit;
            //FileUtil.writeIntoFile(logHome + SEPARATOR + repoUuid + ".txt", content);

            renameHandle(preRawIssues, curFile2PreFile);

            // 为 current raw issues 生成 raw issue match info,没有匹配上的默认状态设置为ADD
            curRawIssues.stream().filter(rawIssue -> !rawIssue.isMapped())
                    .forEach(curRawIssue -> curRawIssue.getMatchInfos().add(curRawIssue.generateRawIssueMatchInfo(parentCommit)));

            // 获取 pre raw issues 关联的 issues
            List<String> oldIssuesUuid = preRawIssues.stream().map(RawIssue::getIssueId).collect(Collectors.toList());
            Map<String, Issue> oldIssuesMap = oldIssuesUuid.isEmpty() ? new HashMap<>(16) :
                    issueDao.getIssuesByUuidAndRepoUuid(oldIssuesUuid, repoUuid).stream().collect(Collectors.toMap(Issue::getUuid, Function.identity(), (oldValue,newValue) -> newValue));

            // 归总结果集, 更新 issues 的 end commit 以及 status
            sumUpRawIssues(parentCommit, preRawIssues, curRawIssues, oldIssuesMap);

            // 为 merge 情况清空 current raw issues 的匹配状态以便下一个 parent commit 匹配
            cleanUpRawIssueMatchInfo(curRawIssues, preRawIssues, parentCommit, isFirst);
        }
        // merge情况 最终归总 raw issue match info
        sumUpAll();

        return matcherResult;
    }

    protected void cleanUpRawIssueMatchInfo(List<RawIssue> curRawIssues, List<RawIssue> preRawIssues, String preCommit, boolean isFirst) {

    }

    protected void sumUpAll() {
        coreService.clearCommit2IssueMap();
        log.info("commit {} sum up finish", currentCommit);
    }

    protected Issue generateOneIssue(RawIssue rawIssue) {
        Issue issue = Issue.valueOf(rawIssue);
        IssueType issueType = issueTypeDao.getIssueTypeByTypeName(rawIssue.getType());
        issue.setIssueCategory(issueType == null ? IssuePriorityEnums.getIssueCategory(rawIssue.getTool(), rawIssue.getPriority()) : issueType.getCategory());
        rawIssue.setIssueId(issue.getUuid());
        rawIssue.getMatchInfos().clear();
        rawIssue.getMatchInfos().add(rawIssue.generateRawIssueMatchInfo(null));
        return issue;
    }
}
