package cn.edu.fudan.violation.core.analyzer;

import cn.edu.fudan.violation.component.RestInterfaceManager;
import cn.edu.fudan.violation.domain.dbo.Location;
import cn.edu.fudan.violation.domain.dbo.LogicalStatement;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.enums.RawIssueStatus;
import cn.edu.fudan.violation.domain.enums.ToolEnum;
import cn.edu.fudan.violation.util.*;
import cn.edu.fudan.violation.util.stat.LogicalStatementUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-20 15:55
 **/
@Slf4j
@Component
@Scope("prototype")
public class SonarQubeBaseAnalyzer extends BaseAnalyzer {

    private static final String COMPONENT = "component";
    private static final String KEY = "key";
    private static DeveloperUniqueNameUtil developerUniqueNameUtil;
    private static RestInterfaceManager restInterfaceManager;

    @Value("${SonarqubeLogHome}")
    private String logHome;

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
        deleteSonarProject(repoUuid + "_" + commit);
        //todo 等待此条扫描结束
        return ShUtil.executeCommand(binHome + "executeSonar.sh " + repoPath + " " + repoUuid + "_" + commit + " " + commit, 50000);
//        return true;
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit, JGitHelper jGitHelper) {
        long analyzeStartTime = System.currentTimeMillis();
        String componentKeys = repoUuid + "_" + commit;
        boolean analyzeResult = true;
        boolean isChanged = false;
        int num;
        final int javaFileNum = FileUtil.getJavaFileNum(repoPath);
        if (javaFileNum == 0) {
            num = 2;
        } else {
            num = Math.min(100, javaFileNum/10+1);
        }
        //轮询 是否扫描完成
        JSONObject results = restInterfaceManager.getSonarIssueResults(componentKeys, null, null, null, 1, false, 0);
        JSONObject securityHotspotsResults = restInterfaceManager.getSonarSecurityHotspotList(componentKeys, 1, 0);
        try {
            // 全量扫描最多等待200秒 根据文件数量动态调整
            for (int i = 1; i <= num; i++) {
                if ((results != null && results.getInteger("total") != 0) ||
                        (securityHotspotsResults != null && securityHotspotsResults.getJSONObject("paging").getInteger("total") != 0)) {
                    isChanged = true;
                    long analyzeEndTime2 = System.currentTimeMillis();
                    log.info("It takes {}s to wait for the latest sonar result ", (analyzeEndTime2 - analyzeStartTime) / 1000);
                    break;
                }
                TimeUnit.SECONDS.sleep(2);
                results = restInterfaceManager.getSonarIssueResults(componentKeys, null, null, null, 1, false, 0);
                securityHotspotsResults = restInterfaceManager.getSonarSecurityHotspotList(componentKeys, 1, 0);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        try {
            //数量过大时 需要等待一定时间确保数量正确
            TimeUnit.SECONDS.sleep(javaFileNum/100);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        results = restInterfaceManager.getSonarIssueResults(componentKeys, null, null, null, 1, false, 0);
        securityHotspotsResults = restInterfaceManager.getSonarSecurityHotspotList(componentKeys, 1, 0);
        //判断是否确实issue为0,还是没获取到这个commit的sonar结果
        if (!isChanged) {
            JSONObject sonarAnalysisTime = restInterfaceManager.getSonarAnalysisTime(repoUuid + "_" + commit);
            if (sonarAnalysisTime.containsKey(COMPONENT)) {
                isChanged = true;
                try {
                    log.info("the number of issue is 0,but get sonar analysis time,sonar result should be changed");
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
        //此时isChanged == false则认为解析失败
        if (!isChanged) {
            log.error("get path {} latest sonar result failed!", concatPath(repoUuid, commit, null, null));
            return false;
        }
        //当获取到total的数量，说明sonar本身的解析完成，开始通过api分条件进行查询
        if (results == null || securityHotspotsResults == null)  {
            return false;
        }
        if (results.getInteger("total") > 10000) {
            final List<String> directoryArr = FileUtil.getJavaDirectories(repoPath);
            // 遍历每个包含.java文件的目录
            for (String d : directoryArr) {
                //绝对路径
                if (!analyzeDirectory(repoPath, repoUuid, commit, d, jGitHelper.getRepoPath())) {
                    analyzeResult = false;
                    break;
                }
            }
        } else {
            // commit级数据 <= 10000条
            analyzeResult = analyze(repoPath, repoUuid, commit, null, null, jGitHelper.getRepoPath(), false);
        }
        analyzeResult = analyzeResult && analyze(repoPath, repoUuid, commit, null, null, jGitHelper.getRepoPath(), true);
        log.info("It takes {}s to wait for the latest sonar result ", (System.currentTimeMillis() - analyzeStartTime) / 1000);
        return analyzeResult;
    }

    private boolean analyzeDirectory(String repoPath, String repoUuid, String commit, String directory, String jgitRepoPath) {
        JSONObject fileArr = restInterfaceManager.getSonarIssueFileUuidsInDirectory(
                repoUuid + "_" + commit, directory.substring(repoPath.length()+1));
        if (fileArr.getInteger("total") > 10000) {
            // > 10000，遍历文件夹文件（不包括子文件夹）
            List<String> fileList = FileUtil.getJavaFiles(directory);
            for (String f : fileList) {
                // 存在一个 file 分析失败，则整个目录的分析结果也失败
                if (!analyze(repoPath, repoUuid, commit, null, f.substring(repoPath.length()+1), jgitRepoPath, false)) {
                    return false;
                }
            }
        } else {
            // <= 10000 目录（不包括子目录）
            return analyze(repoPath, repoUuid, commit, directory.substring(repoPath.length()+1), null, jgitRepoPath, false);
        }
        return false;
    }

    private boolean analyze(String repoPath, String repoUuid, String commit, String directories, String fileUuids, String jgitRepoPath, boolean isSecurityHotspots) {
        //解析sonar的issues为平台的rawIssue
        boolean getRawIssueSuccess = getSonarResult(repoUuid, commit, repoPath, directories, fileUuids, jgitRepoPath, isSecurityHotspots);

        log.info("Current path {}, rawIssue total is {}", concatPath(repoUuid, commit, directories, fileUuids), resultRawIssues.size());
        if (!getRawIssueSuccess) {
            log.error("get {} raw issues failed", concatPath(repoUuid, commit, directories, fileUuids));
            return false;
        }
        return true;
    }

    private boolean getSonarResult(String repoUuid, String commit, String repoPath,
                                   String directories, String fileUuids, String jgitRepoPath, boolean isSecurityHotspots) {
        String componentKeys = repoUuid + "_" + commit;
        List<String> parentCommits = commitToParentCommits.getOrDefault(commit, new ArrayList<>());
        JSONObject sonarIssueResult;
        //获取issue数量
        if(!isSecurityHotspots) {
            sonarIssueResult = restInterfaceManager.getSonarIssueResults(
                    componentKeys, directories, fileUuids, null, 1, false, 0);
        } else {
            sonarIssueResult = restInterfaceManager.getSonarSecurityHotspotList(componentKeys, 1, 0);
        }
        List<RawIssue> tempRawIssues = new ArrayList<>();
        try {
            int pageSize = 100;
            int issueTotal;
            if(!isSecurityHotspots) {
                issueTotal = sonarIssueResult.getIntValue("total");
            } else {
                issueTotal = sonarIssueResult.getJSONObject("paging").getIntValue("total");
            }
            log.info("Current path {}, issueTotal in sonar result is {}", concatPath(repoUuid, commit, directories, fileUuids), issueTotal);
            if (issueTotal > 10000) {
                log.warn("issue in directories:{}, fileUuids:{} is larger than 10000", directories, fileUuids);
            }
            //分页取sonar的issue 超过1w条只取前1w条
            int pages = Math.min(issueTotal % pageSize > 0 ? issueTotal / pageSize + 1 : issueTotal / pageSize, 100);
            for (int i = 1; i <= pages; i++) {
                JSONObject sonarResult;
                JSONArray sonarRawIssues;
                if(!isSecurityHotspots) {
                    sonarResult = restInterfaceManager.getSonarIssueResults(
                            componentKeys, directories, fileUuids, null, pageSize, false, i);
                    sonarRawIssues = sonarResult.getJSONArray("issues");
                } else {
                    sonarResult = restInterfaceManager.getSonarSecurityHotspotList(componentKeys, pageSize, i);
                    sonarRawIssues = sonarResult.getJSONArray("hotspots");
                }
                //解析sonar的issues为平台的rawIssue
                for (int j = 0; j < sonarRawIssues.size(); j++) {
                    JSONObject sonarIssue = sonarRawIssues.getJSONObject(j);
                    String component;
                    if(isSecurityHotspots) {
                        sonarIssue = restInterfaceManager.getSonarSecurityHotspot(sonarIssue.getString(KEY));
                        component = sonarIssue.getJSONObject(COMPONENT).getString(KEY);
                    } else {
                        component = sonarIssue.getString(COMPONENT);
                    }
                    //仅解析java文件且非test文件夹
                    if (FileFilter.javaFilenameFilter(component)) {
                        continue;
                    }
                    //解析location
                    List<Location> locations = getLocations(sonarIssue, repoPath, repoUuid, isSecurityHotspots);
                    if (locations.isEmpty()) {
                        continue;
                    }
                    //解析rawIssue
                    RawIssue rawIssue = getRawIssue(repoUuid, commit, ToolEnum.SONAR.getType(), sonarIssue, repoPath, jgitRepoPath, parentCommits, isSecurityHotspots);
                    locations.forEach(location -> location.setFilePath(rawIssue.getFileName()));
                    rawIssue.setLocations(locations);
                    rawIssue.setStatus(RawIssueStatus.ADD.getType());
                    String rawIssueUuid = RawIssue.generateRawIssueUUID(rawIssue);
                    String rawIssueHash = RawIssue.generateRawIssueHash(rawIssue);
                    rawIssue.setUuid(rawIssueUuid);
                    rawIssue.setRawIssueHash(rawIssueHash);
                    locations.forEach(location -> location.setRawIssueUuid(rawIssueUuid));
                    tempRawIssues.add(rawIssue);
                }
            }
            final Map<String, List<RawIssue>> file2RawIssuesMap = tempRawIssues.stream().collect(Collectors.groupingBy(rawIssue -> rawIssue.getLocations().get(0).getScanFilePath()));
            for (Map.Entry<String, List<RawIssue>> file2RawIssues : file2RawIssuesMap.entrySet()) {
                final List<RawIssue> rawIssues = file2RawIssues.getValue();
                final List<Location> locations = rawIssues.stream().map(RawIssue::getLocations).flatMap(Collection::stream).collect(Collectors.toList());
                final List<Integer> beginLines = locations.stream().map(Location::getStartLine).collect(Collectors.toList());
                final List<Integer> endLines = locations.stream().map(Location::getEndLine).collect(Collectors.toList());
                final List<Integer> startTokens = locations.stream().map(Location::getStartToken).collect(Collectors.toList());
                String realFilePath = repoPath + "/" + locations.get(0).getScanFilePath();
                log.info("cur file  {}, rawIssueTotal is {}", realFilePath, rawIssues.size());
                List<String> codeList;
                List<String> anchorNameList;
                List<Integer> anchorOffsetList;
                List<String> classNameList;
                try {
                    List<LogicalStatement> logicalStatements = LogicalStatementUtil.getLogicalStatements(realFilePath, beginLines, endLines, startTokens);
                    codeList = logicalStatements.stream().map(LogicalStatement::getContent).collect(Collectors.toList());
                    anchorNameList = logicalStatements.stream().map(LogicalStatement::getAnchorName).collect(Collectors.toList());
                    anchorOffsetList = logicalStatements.stream().map(LogicalStatement::getAnchorOffset).collect(Collectors.toList());
                    classNameList = logicalStatements.stream().map(LogicalStatement::getClassName).collect(Collectors.toList());
                    for (int i1 = 0; i1 < locations.size(); i1++) {
                        final Location location = locations.get(i1);
                        location.setAnchorName(anchorNameList.get(i1));
                        location.setOffset(anchorOffsetList.get(i1));
                        location.setClassName(classNameList.get(i1));
                        location.setCode(codeList.get(i1));
                    }
                } catch (Exception e) {
                    log.error("parse file {} failed! rawIssue num is {}", realFilePath, rawIssues.size());
                    log.error("parse message: {} ...", StringsUtil.firstLine(e.getMessage()));
                    // 排除当前文件的 rawIssue 数据，继续解析下一个文件数据
                    rawIssues.forEach(rawIssue -> tempRawIssues.removeIf(tempRawIssue -> tempRawIssue.getUuid().equals(rawIssue.getUuid())));
                }
            }
            resultRawIssues.addAll(tempRawIssues);
            return true;
        } catch (Exception e) {
            log.error("getSonarResult message:{}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void deleteSonarProject(String projectName) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + "deleteSonarProject.sh " + projectName + " " + DatatypeConverter.printBase64Binary((restInterfaceManager.sonarLogin + ":" + restInterfaceManager.sonarPassword).getBytes(StandardCharsets.UTF_8));
            log.info("command -> {}", command);
            if (rt.exec(command).waitFor() == 0) {
                log.info("delete sonar project:{} success! ", projectName);
            }
        } catch (Exception e) {
            log.error("delete sonar project:{},cause:{}", projectName, e.getMessage());
        }
    }

    @Override
    public String getToolName() {
        return ToolEnum.SONAR.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        int result = 1;
        String detail = rawIssue.getDetail();
        String[] rawIssueArgs = detail.split("---");
        String severity = rawIssueArgs[rawIssueArgs.length - 1];
        switch (severity) {
            case "BLOCKER":
                result = 0;
                break;
            case "CRITICAL":
                result = 1;
                break;
            case "MAJOR":
                result = 2;
                break;
            case "MINOR":
                result = 3;
                break;
            case "INFO":
                result = 4;
                break;
            default:
        }
        return result;
    }

    public List<Location> getLocations(JSONObject issue, String repoPath, String repoUuid, boolean isSecurityHotspots) {
        int startLine;
        int endLine;
        int startOffset;
        int endOffset;
        String sonarPath;
        String[] sonarComponents;
        String filePath = null;
        List<Location> locations = new ArrayList<>();
        JSONArray flows = issue.getJSONArray("flows");
        if (flows == null || flows.isEmpty()) {
            //第一种针对issue中的textRange存储location
            JSONObject textRange = issue.getJSONObject("textRange");
            if (textRange != null) {
                startLine = textRange.getIntValue("startLine");
                endLine = textRange.getIntValue("endLine");
                startOffset = textRange.getIntValue("startOffset");
                endOffset = textRange.getInteger("endOffset");
            } else {
                // 无 location 行号信息的 issue 过滤掉
                return new ArrayList<>();
            }

            if (!isSecurityHotspots) {
                sonarPath = issue.getString(COMPONENT);
            } else {
                sonarPath = issue.getJSONObject(COMPONENT).getString(KEY);
            }
            if (sonarPath != null) {
                sonarComponents = sonarPath.split(":");
                if (sonarComponents.length >= 2) {
                    filePath = sonarComponents[sonarComponents.length - 1];
                }
            }

            Location mainLocation = getLocation(startLine, endLine, filePath, repoPath, repoUuid, startOffset, endOffset);
            locations.add(mainLocation);
        } else {
            //第二种针对issue中的flows中的所有location存储
            for (int i = 0; i < flows.size(); i++) {
                JSONObject flow = flows.getJSONObject(i);
                JSONArray flowLocations = flow.getJSONArray("locations");
                //一个flows里面有多个locations， locations是一个数组，目前看sonar的结果每个locations都是一个location，但是不排除有多个。
                for (int j = 0; j < flowLocations.size(); j++) {
                    JSONObject flowLocation = flowLocations.getJSONObject(j);
                    String flowComponent = flowLocation.getString(COMPONENT);
                    JSONObject flowTextRange = flowLocation.getJSONObject("textRange");
                    if (flowTextRange == null || flowComponent == null) {
                        continue;
                    }
                    int flowStartLine = flowTextRange.getIntValue("startLine");
                    int flowEndLine = flowTextRange.getIntValue("endLine");
                    int flowStartOffset = flowTextRange.getIntValue("startOffset");
                    int flowEndOffset = flowTextRange.getIntValue("endOffset");
                    String flowFilePath = null;

                    String[] flowComponents = flowComponent.split(":");
                    if (flowComponents.length >= 2) {
                        flowFilePath = flowComponents[flowComponents.length - 1];
                    }

                    Location location = getLocation(flowStartLine, flowEndLine, flowFilePath, repoPath, repoUuid, flowStartOffset, flowEndOffset);
                    locations.add(location);
                }
            }
        }

        return locations;
    }

    private Location getLocation(int startLine, int endLine, String filePath, String repoPath, String repoUuid, int startToken, int endToken) {
        Location location = new Location();
        assert filePath != null;
        String relativePath = filePath;
        location.setFilePath(filePath);
        //不是全量扫描的时候需要去掉开头的commit
        if (!isTotalScan) {
            relativePath = filePath.substring(41);
            location.setFilePath(relativePath);
        }
        String locationUuid = Location.generateLocationUUID(repoUuid, relativePath, startLine, endLine, startToken, endToken);

        location.setScanFilePath(filePath);
        location.setUuid(locationUuid);
        location.setStartLine(startLine);
        location.setEndLine(endLine);
        location.setStartToken(startToken);
        location.setEndToken(endToken);
        if (startLine > endLine) {
            log.error("startLine > endLine,fileName is {},startLine is {},endLine is {}", filePath, startLine, endLine);
            int temp = startLine;
            startLine = endLine;
            endLine = temp;
        }
        location.setBugLines(startLine + "-" + endLine);

        return location;
    }

    private RawIssue getRawIssue(String repoUuid, String commit, String category, JSONObject issue, String repoPath,
                                 String jgitRepoPath, List<String> parentCommits, boolean isSecurityHotspots) {
        //根据ruleId获取rule的name
        String issueName = null;
        String issueType;
        if (!isSecurityHotspots) {
            issueType = issue.getString("rule");
        } else {
            issueType = issue.getJSONObject("rule").getString("key");
        }
        JSONObject rule = restInterfaceManager.getRuleInfo(issueType, null, null);
        if (rule != null) {
            issueName = rule.getJSONObject("rule").getString("name");
        }
        //获取文件路径
        String[] sonarComponents;
        String filePath = null;
        String sonarPath;
        if (!isSecurityHotspots) {
            sonarPath = issue.getString(COMPONENT);
        } else {
            sonarPath = issue.getJSONObject(COMPONENT).getString("key");
        }
        if (sonarPath != null) {
            sonarComponents = sonarPath.split(":");
            if (sonarComponents.length >= 2) {
                filePath = sonarComponents[sonarComponents.length - 1];
            }
        }
        RawIssue rawIssue = new RawIssue();
        rawIssue.setTool(category);
        rawIssue.setType(issueName);
        rawIssue.setFileName(filePath);
        rawIssue.setCommitId(commit);
        //当parentCommit为空的时候，说明是全量扫描
        if (!isTotalScan) {
            assert filePath != null;
            for (String parentCommit : parentCommits) {
                if (filePath.contains(parentCommit)) {
                    rawIssue.setCommitId(parentCommit);
                    break;
                }
            }
            rawIssue.setFileName(filePath.substring(41));
        }
        String severity = null;
        if(rule != null) {
            severity = rule.getJSONObject("rule").getString("severity");
        }
        rawIssue.setDetail(issue.getString("message") + "---" + severity);
        // fixme 待改，因为数据库不可为空
        rawIssue.setScanId(ToolEnum.SONAR.getType());
        rawIssue.setRepoUuid(repoUuid);

        String developerUniqueName = developerUniqueNameUtil.getDeveloperUniqueName(jgitRepoPath, commit, repoUuid);

        rawIssue.setDeveloperName(developerUniqueName);
        rawIssue.setPriority(getPriorityByRawIssue(rawIssue));
        return rawIssue;
    }

    private String concatPath(String repoUuid, String commit, String directories, String fileUuids) {
        StringBuilder sb = new StringBuilder(repoUuid + "_" + commit);
        if (directories != null) {
            sb.append(" ").append(directories);
        }
        if (fileUuids != null) {
            sb.append(" ").append(fileUuids);
        }
        return sb.toString();
    }

    @Override
    public boolean fileFilter(String fileName) {
        return FileFilter.javaFilenameFilter(fileName);
    }

    @Override
    public boolean closeResourceLoader() {
        return false;
    }

    @Override
    public Set<String> getMethodsAndFieldsInFile(String absoluteFilePath) throws IOException {
        Set<String> methodsAndFields = new HashSet<>();
        List<String> allFieldsInFile = AstParserUtil.getAllFieldsInFile(absoluteFilePath);
        List<String> allMethodsInFile = AstParserUtil.getAllMethodsInFile(absoluteFilePath);

        methodsAndFields.addAll(allFieldsInFile);
        methodsAndFields.addAll(allMethodsInFile);

        return methodsAndFields;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        SonarQubeBaseAnalyzer.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setDeveloperUniqueNameUtil(DeveloperUniqueNameUtil developerUniqueNameUtil) {
        SonarQubeBaseAnalyzer.developerUniqueNameUtil = developerUniqueNameUtil;
    }

}
