package cn.edu.fudan.violation.core.service;

import cn.edu.fudan.violation.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.violation.core.process.RawIssueMatcher;
import cn.edu.fudan.violation.dao.*;
import cn.edu.fudan.violation.domain.dbo.Location;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.enums.SolveWayEnum;
import cn.edu.fudan.violation.util.JGitHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author beethoven
 * @date 2021-09-24 09:18:19
 */
@Service
@Slf4j
public class CoreService {

    private final Map<String, Set<String>> commit2IssueUuidMap = new HashMap<>();
    private RawIssueDao rawIssueDao;
    private IssueAnalyzerDao issueAnalyzerDao;
    private IssueDao issueDao;
    private RawIssueMatchInfoDao rawIssueMatchInfoDao;
    private LocationDao locationDao;

    public boolean checkParentCommitHasIssue(JGitHelper jGitHelper, String commit, String issueUuid, String repoUuid) {
        List<String> allCommitParents = jGitHelper.getAllCommitParents(commit);
        List<String> statuses = rawIssueMatchInfoDao.getMatchInfoByIssueUuidAndCommitsAndRepo(issueUuid, allCommitParents, repoUuid);
        if (!statuses.isEmpty()) {
            return true;
        }
        return false;
    }

    public List<RawIssue> matchNewIssuesWithParentIssues(List<RawIssue> curRawIssues, Map<String, String> curFile2PreFiles, List<String> parentCommits, String repoUuid, BaseAnalyzer baseAnalyzer, String repoPath){
        Map<String, List<RawIssue>> fileName2RawIssues = curRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));
        List<RawIssue> preRawIssuesNeedToStore = new ArrayList<>();
        for (Map.Entry<String, List<RawIssue>> stringListEntry : fileName2RawIssues.entrySet()) {
            String fileName = stringListEntry.getKey();
            List<RawIssue> rawIssues = stringListEntry.getValue();
            Set<String> methodsAndFieldsInFile = Collections.emptySet();
            try{
                methodsAndFieldsInFile = baseAnalyzer.getMethodsAndFieldsInFile(repoPath + "/" + fileName);
            } catch (IOException e) {
                log.error(e.getMessage());
            }

            List<String> issueTypes = rawIssues.stream().map(RawIssue::getType).distinct().collect(Collectors.toList());
            List<String> solvedIssues = issueDao.getSolvedIssuesByTypesAndFile(repoUuid, issueTypes, curFile2PreFiles.get(fileName));
            List<RawIssue> preRawIssues = rawIssueDao.getLastVersionRawIssues(parentCommits, solvedIssues);
            preRawIssues.forEach(rawIssue -> rawIssue.setLocations(locationDao.getLocations(rawIssue.getUuid())));
            RawIssueMatcher.match(preRawIssues,rawIssues,methodsAndFieldsInFile);
            preRawIssuesNeedToStore.addAll(preRawIssues.stream().filter(RawIssue::isMapped).collect(Collectors.toList()));
        }
        return preRawIssuesNeedToStore;
    }

    public boolean checkOneCommitHasIssue(String repoUuid, String commit, String issueUuid, String tool, Set<String> filenames) {
        if (!commit2IssueUuidMap.containsKey(commit)) {
            commit2IssueUuidMap.put(commit, getIssueUuidsInCommit(repoUuid, commit, tool, filenames));
        }
        Set<String> issueUuidSet = commit2IssueUuidMap.get(commit);
        return issueUuidSet.contains(issueUuid);
    }

    private Set<String> getIssueUuidsInCommit(String repoUuid, String commit, String tool, Set<String> filenames) {
        JSONObject analyzeResult = issueAnalyzerDao.getAnalyzeResultByRepoUuidCommitIdTool(repoUuid, commit, tool);
        if (analyzeResult == null) {
            log.error("{} raw issue cache is null", repoUuid + "_" + commit);
            return new HashSet<>();
        }
        List<RawIssue> rawIssues = new ArrayList<>();
        Map<String, Object> result = analyzeResult.getJSONObject("analyzeResult");
        if (result != null) {
            for (Object value : result.values()) {
                String s = JSON.toJSONString(value);
                rawIssues.addAll(JSON.parseArray(s, RawIssue.class));
            }
        }
        List<String> list = new ArrayList<>();
        for (RawIssue rawIssue : rawIssues) {
            if (filenames.contains(rawIssue.getFileName())) {
                list.add(RawIssue.generateRawIssueHash(rawIssue));
            }
        }
        return rawIssueDao.getIssueUuidsByRawIssueHashs(list,repoUuid);
    }

    public SolveWayEnum checkHowToSolved(BaseAnalyzer baseAnalyzer, JGitHelper jGitHelper,
                                         RawIssue preRawIssue, List<DiffEntry> diffs) {
        String curFilename = null;
        for (DiffEntry diff : diffs) {
            if (diff.getOldPath().equals(preRawIssue.getFileName())) {
                if (diff.getChangeType().equals(DiffEntry.ChangeType.DELETE))
                    return SolveWayEnum.FILE_DELETE;
                else {
                    curFilename = diff.getNewPath();
                    break;
                }
            }
        }

        if (curFilename == null)
            return SolveWayEnum.EXCEPTION;

        return checkIsMethodDelete(baseAnalyzer, jGitHelper.getRepoPath(), curFilename, preRawIssue.getLocations());
    }

    public SolveWayEnum checkIsMethodDelete(BaseAnalyzer baseAnalyzer, String repoPath, String curFilename, List<Location> locations) {
        try {
            boolean isExist = checkMethodOrFieldExist(baseAnalyzer, repoPath + "/" + curFilename, locations);
            return isExist ? SolveWayEnum.CODE_CHANGE : SolveWayEnum.ANCHOR_DELETE;
        } catch (Exception e) {
            return SolveWayEnum.EXCEPTION;
        }
    }

    public static boolean checkMethodOrFieldExist(BaseAnalyzer baseAnalyzer, String absoluteFilePath, List<Location> locations) throws IOException {
        int size = locations.size(), num = 0;
        Set<String> methodNameAndFields = baseAnalyzer.getMethodsAndFieldsInFile(absoluteFilePath);
        for (Location location : locations) {
            if (methodNameAndFields.contains(location.getAnchorName())) {
                num++;
            }
        }
        return (num * 1.0 / size) >= 0.5;
    }


    public void clearCommit2IssueMap() {
        commit2IssueUuidMap.clear();
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setIssueAnalyzerDao(IssueAnalyzerDao issueAnalyzerDao) {
        this.issueAnalyzerDao = issueAnalyzerDao;
    }

    @Autowired
    public void setRawIssueMatchInfoDao(RawIssueMatchInfoDao rawIssueMatchInfoDao) {
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }
}
