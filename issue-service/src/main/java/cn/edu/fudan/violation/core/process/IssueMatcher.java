package cn.edu.fudan.violation.core.process;

import cn.edu.fudan.violation.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.violation.core.matcher.Matcher;
import cn.edu.fudan.violation.core.matcher.MatcherFactory;
import cn.edu.fudan.violation.core.service.CoreService;
import cn.edu.fudan.violation.dao.*;
import cn.edu.fudan.violation.domain.dbo.Issue;
import cn.edu.fudan.violation.domain.dbo.IssueScan;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.dto.MatcherCommitInfo;
import cn.edu.fudan.violation.domain.dto.MatcherData;
import cn.edu.fudan.violation.domain.dto.MatcherResult;
import cn.edu.fudan.violation.domain.enums.CommitStatusEnum;
import cn.edu.fudan.violation.domain.enums.ScanStatusEnum;
import cn.edu.fudan.violation.util.FileFilter;
import cn.edu.fudan.violation.util.JGitHelper;
import cn.edu.fudan.violation.util.SearchUtil;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fancying
 * create: 2020-05-20 16:56
 **/
@Slf4j
@Data
@Component
@Scope("prototype")
public class IssueMatcher {

    private static IssueScanDao issueScanDao;
    private static RawIssueDao rawIssueDao;
    private static IssueDao issueDao;
    private static IssueTypeDao issueTypeDao;
    private static LocationDao locationDao;
    private static RawIssueMatchInfoDao rawIssueMatchInfoDao;
    private CoreService coreService;
    private JGitHelper jGitHelper;
    private String curCommit;
    private BaseAnalyzer analyzer;
    private MatcherFactory matcherFactory;
    private int ignoreMergeSolvedIssueNum;
    @Value("${baseLogHome}")
    private String logHome;

    /**
     * des: @key parentCommitId   @value ???curCommit ?????????
     * ??? RawIssueMatchInfo ???????????????
     * ?????? solved??????
     * matchInfoResult RawIssue ????????????????????????
     **/
    @Getter
    private Map<String, List<RawIssue>> parentRawIssuesResult = new HashMap<>(4);

    /**
     * ?????????rawIssue location matchInfoResult
     * rawIssue??????????????? 1 ??????matchInfo???status?????? change ????????? 2 rawIssue ?????????????????? ??? {@link RawIssue#isNotChange}  ??? false???
     * ???todo ??????????????????????????????rawIssue?????????
     * matchInfoResult RawIssue ???????????????????????? ????????? add change mergeSolved ?????????
     **/
    @Getter
    private List<RawIssue> curAllRawIssues;

    /**
     * des: @key issue uuid   @value issue ?????? mapped issue
     * <p>
     * ????????????issue???
     **/
    @Getter
    private Map<String, Issue> mappedIssues;

    /**
     * des: @key issue uuid   @value issue ?????? reopen issue
     * <p>
     * ????????????issue???
     **/
    @Getter
    private Map<String, Issue> reopenIssues;


    /**
     * des: @key issue uuid   @value issue ???????????????issue??????
     * ?????????issue ???????????????
     **/
    @Getter
    private Map<String, Issue> newIssues = new HashMap<>(0);

    /**
     * des: @key issue uuid   @value issue ???????????????issue??????
     * ?????????issue ??? ????????????
     **/
    @Getter
    private Map<String, Issue> solvedIssue;

    private Map<String, Map<String, String>> fileMap = new HashMap<>();

    private ApplicationContext applicationContext;

    public boolean matchProcess(String repoUuid, String curCommit, JGitHelper jGitHelper, String tool, List<RawIssue> parentAndCurRawIssues) {
        try {
            // checkout ?????? commit
            this.jGitHelper = jGitHelper;
            jGitHelper.checkout(curCommit);

            // ????????????????????? parent commits ???????????? matcher data
            List<String> parentCommits = getPreScanSuccessfullyCommit(repoUuid, curCommit, jGitHelper, tool);
            //?????????????????????????????????
            MatcherData matcherData = initMatcherData(repoUuid, curCommit, jGitHelper, tool, parentAndCurRawIssues, parentCommits);

            // ?????? commit ??? first normal or merge
            CommitStatusEnum commitStatusEnum = CommitStatusEnum.FIRST;
            if (!parentCommits.isEmpty()) {
                commitStatusEnum = parentCommits.size() > 1 ? CommitStatusEnum.MERGE : CommitStatusEnum.NORMAL;
            }

            // ?????? Matcher ?????????
            Matcher matcher = matcherFactory.createMatcher(commitStatusEnum, matcherData);
            matcher.init(logHome, issueDao, issueTypeDao, rawIssueDao, rawIssueMatchInfoDao, coreService);
            MatcherResult matcherResult = matcher.matchRawIssues();

            // ?????? matcher data
            sumUpMatcherData(matcherData, matcherResult);
        } catch (Exception e) {
            log.error("match throw exception, check the code");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private MatcherData initMatcherData(String repoUuid, String curCommit, JGitHelper jGitHelper, String tool, List<RawIssue> parentAndCurRawIssues, List<String> parentCommits) {
        log.info("start init matcher data for current commit: {}", curCommit);
        Set<RawIssue> curRawIssueSet = new HashSet<>();
        Map<String, List<String>> commitFileMap = new HashMap<>(8);
        Map<String, List<RawIssue>> parentRawIssuesMap = new HashMap<>(8);
        Map<String, Map<String, String>> preFile2CurFileMap = new HashMap<>(8);
        Map<String, Map<String, String>> curFile2PreFileMap = new HashMap<>(8);

        Map<String, List<RawIssue>> commitToRawIssues = parentAndCurRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getCommitId));

        List<RawIssue> curRawIssues = commitToRawIssues.getOrDefault(curCommit, new ArrayList<>());

//        curRawIssueSet.addAll(curRawIssues);
        if (parentCommits.isEmpty()) {
            curRawIssueSet.addAll(curRawIssues);
        }

        for (String parentCommit : parentCommits) {
            Map<String, String> preFile2CurFile = new HashMap<>(16);
            Map<String, String> curFile2PreFile = new HashMap<>(16);

            // ????????? diff ?????????
            List<String> diffFiles = jGitHelper.getDiffFilePair(parentCommit, curCommit, preFile2CurFile, curFile2PreFile);

            // ?????? diff ?????????
            var delimiter = ",";
            List<String> preFiles = diffFiles.stream().filter(d -> !d.startsWith(delimiter)).map(f -> Arrays.asList(f.split(delimiter)).get(0)).collect(Collectors.toList());
            List<String> curFiles = diffFiles.stream().filter(d -> !d.endsWith(delimiter)).map(f -> Arrays.asList(f.split(delimiter)).get(1)).collect(Collectors.toList());

            // ????????????
            curFiles = curFiles.stream().filter(file -> !FileFilter.fileFilter(tool, file)).collect(Collectors.toList());

            preFiles = preFiles.stream().filter(file -> !FileFilter.fileFilter(tool, file)).collect(Collectors.toList());
            List<String> finalCurFiles = curFiles;
            curRawIssueSet.addAll(curRawIssues.stream().filter(rawIssue -> finalCurFiles.contains(rawIssue.getFileName())).collect(Collectors.toList()));

            List<RawIssue> preRawIssues = commitToRawIssues.getOrDefault(parentCommit, new ArrayList<>());
            List<String> preCommitsForParent = jGitHelper.getAllCommitParents(parentCommit);

            List<String> finalPreFiles = preFiles;
            preRawIssues = preRawIssues.stream().filter(rawIssue -> finalPreFiles.contains(rawIssue.getFileName())).collect(Collectors.toList());
            //??????rawIssueHash???parentCommit?????????rawIssue?????????issue uuid
            preRawIssues.forEach(rawIssue -> {
                String issueId = rawIssueDao.getIssueUuidByRawIssueHashAndParentCommits(repoUuid, rawIssue.getRawIssueHash(), preCommitsForParent);
                if (issueId == null) {
                    log.warn("hash: " + rawIssue.getRawIssueHash() + " is null, commit id: {}", rawIssue.getCommitId());
                }
                rawIssue.setIssueId(issueId);
            });

            commitFileMap.put(parentCommit, curFiles);
            parentRawIssuesMap.put(parentCommit, preRawIssues);
            preFile2CurFileMap.put(parentCommit, preFile2CurFile);
            curFile2PreFileMap.put(parentCommit, curFile2PreFile);
        }
        this.fileMap = preFile2CurFileMap;
        log.info("init matcher data success!");
        return MatcherData.builder()
                .tool(tool)
                .repoUuid(repoUuid)
                .currentCommit(curCommit)
                .analyzer(analyzer)
                .jGitHelper(jGitHelper)
                .commitFileMap(commitFileMap)
                .currentRawIssues(new ArrayList<>(curRawIssueSet))
                .parentRawIssuesMap(parentRawIssuesMap)
                .preFile2CurFileMap(preFile2CurFileMap)
                .curFile2PreFileMap(curFile2PreFileMap)
                .build();
    }

    private void sumUpMatcherData(MatcherData matcherData, MatcherResult matcherResult) {
        this.curAllRawIssues = matcherData.getCurrentRawIssues();
        this.newIssues = matcherResult.getNewIssues();
        this.mappedIssues = matcherResult.getMappedIssues();
        this.solvedIssue = matcherResult.getSolvedIssue();
        this.reopenIssues = matcherResult.getReopenIssues();
        this.parentRawIssuesResult = matcherResult.getParentRawIssuesResult();
        this.ignoreMergeSolvedIssueNum = matcherResult.getIgnoreSolvedMergeIssueNum();
    }

    public List<String> getPreScanSuccessfullyCommit(String repoId, String commitId, JGitHelper jGitHelper, String tool) throws IOException {

        List<IssueScan> scanList = issueScanDao.getIssueScanByRepoIdAndStatusAndTool(repoId, null, tool);
        if (scanList == null || scanList.isEmpty()) {
            return new ArrayList<>(0);
        }

        String[] scannedCommitIds = scanList.stream().map(IssueScan::getCommitId).toArray(String[]::new);

        List<MatcherCommitInfo> parentCommits = new ArrayList<>();
        parentCommits.add(new MatcherCommitInfo(repoId, commitId, jGitHelper.getCommitTime(commitId)));

        Set<String> scannedParents = new HashSet<>(8);
        while (!parentCommits.isEmpty()) {
            MatcherCommitInfo matcherCommitInfo = parentCommits.remove(0);
            String[] parents = jGitHelper.getCommitParents(matcherCommitInfo.getCommitId());

            for (String parent : parents) {
                int index = SearchUtil.dichotomy(scannedCommitIds, parent);
                if (index == -1) {
                    continue;
                }

                if (ScanStatusEnum.DONE.getType().equals(scanList.get(index).getStatus())) {
                    scannedParents.add(parent);
                    continue;
                }
                parentCommits.add(new MatcherCommitInfo(repoId, parent, jGitHelper.getCommitTime(commitId)));
                // parentCommits ?????????????????????????????? ???????????????????????????
                parentCommits = parentCommits.stream().distinct()
                        .sorted(Comparator.comparing(MatcherCommitInfo::getCommitTime)).collect(Collectors.toList());
            }
        }

        // ??????????????????????????????parent
        List<String> trueParents = removeFakeParent(scannedParents);

        return new ArrayList<>(trueParents);
    }

    public List<String> removeFakeParent(Set<String> scannedParents) throws IOException {
        List<String> list = new ArrayList<>(scannedParents);
        boolean[] fakeParent = new boolean[scannedParents.size()];

        list.sort((o1, o2) -> jGitHelper.getLongCommitTime(o2).compareTo(jGitHelper.getLongCommitTime(o1)));
        for (int i = 0; i < list.size(); i++) {
            if (!fakeParent[i]) {
                for (int j = i + 1; j < list.size(); j++) {
                    if (jGitHelper.isParent(list.get(i), list.get(j))) {
                        fakeParent[j] = true;
                    }
                }
            }
        }

        List<String> trueParents = new ArrayList<>();
        for (int i = 0; i < fakeParent.length; i++) {
            if (!fakeParent[i]) {
                trueParents.add(list.get(i));
            }
        }

        return trueParents;
    }

    public void cleanParentRawIssueResult() {
        parentRawIssuesResult.clear();
    }

    @Autowired
    public void setMatcherFactory(MatcherFactory matcherFactory) {
        this.matcherFactory = matcherFactory;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setIssueTypeDao(IssueTypeDao issueTypeDao) {
        this.issueTypeDao = issueTypeDao;
    }

    @Autowired
    public void setRawIssueMatchInfoDao(RawIssueMatchInfoDao rawIssueMatchInfoDao) {
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
    }

    @Autowired
    public void setCoreService(CoreService coreService) {
        this.coreService = coreService;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
