package cn.edu.fudan.violation.core.analyzer;

import cn.edu.fudan.codetracker.scan.core.tree.parser.CppFileParser;
import cn.edu.fudan.codetracker.scan.domain.NodeType;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.FieldNode;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.FileNode;
import cn.edu.fudan.codetracker.scan.domain.projectinfo.MethodNode;
import cn.edu.fudan.violation.component.RestInterfaceManager;
import cn.edu.fudan.violation.dao.IssueAnalyzerDao;
import cn.edu.fudan.violation.domain.dbo.Location;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.domain.dto.XmlError;
import cn.edu.fudan.violation.domain.enums.IssuePriorityEnums.CppIssuePriorityEnum;
import cn.edu.fudan.violation.domain.enums.ToolEnum;
import cn.edu.fudan.violation.util.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

/**
 * @author beethoven
 * @date 2021-07-01 11:11:46
 */
@Slf4j
@Component
@Scope("prototype")
public class TscanCodeAnalyzer extends BaseAnalyzer {

    private static final String TOOL_NAME = "TscanCode";
    private static IssueAnalyzerDao issueAnalyzerDao;
    private final Map<String, Map<String, int[]>> method2Line = new HashMap<>();
    private final Map<String, Map<String, String>> method2FullName = new HashMap<>();
    private static final String PARSE_METHOD_NAME_FAIL = "Tscancode方法名解析错误";
    @Value("${binHome}")
    private String binHome;
    @Value("${TscanCodeLogHome}")
    private String tscanLogHome;
    private DeveloperUniqueNameUtil developerUniqueNameUtil;
    private RestInterfaceManager restInterfaceManager;

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
//
        return ShUtil.executeCommand(binHome + "executeTscanCode.sh " + repoPath + " " + repoUuid + " " + commit, 50000);
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit, JGitHelper jGitHelper) {

        String errFile = tscanLogHome + "err-" + repoUuid + "_" + commit + ".xml";
        String infoFile = tscanLogHome + "info-" + repoUuid + "_" + commit + ".txt";

        try {
            List<XmlError> errors = XmlUtil.getError(errFile);
            if (!debugMode){
                ShUtil.executeCommand(binHome + "deleteScanResult.sh " + errFile + " " + infoFile, 2000);
            }
            return xmlErrors2RawIssues(errors, commit, repoUuid, repoPath, jGitHelper);
        } catch (IOException | SAXException | JDOMException e) {
            log.error("parse xml file failed, fileName is: {}", errFile);
            log.error("exception msg is: {}", e.getMessage());
            return false;
        }
    }

    private boolean xmlErrors2RawIssues(List<XmlError> errors, String commit, String repoUuid, String repoPath, JGitHelper jGitHelper) {

        String jgitRepoPath = jGitHelper.getRepoPath();

        List<RawIssue> rawIssues = new ArrayList<>();

        try {

            String developerUniqueName = developerUniqueNameUtil.getDeveloperUniqueName(jgitRepoPath, commit, repoUuid);

            Set<String> files = new HashSet<>();
            errors.forEach(error -> files.add(error.getFile()));
//            boolean isParseSuccess = parseMethodAndField(files, jGitHelper, commit);

            for (XmlError error : errors) {

                String file = error.getFile();

                String relativePath;
                if (isTotalScan) {
                    relativePath = FileUtil.handleFileNameToRelativePath(error.getFile());
                } else {
                    relativePath = file.substring(file.indexOf(commit) + 82);
                }

                RawIssue rawIssue = new RawIssue();
                rawIssue.setType(error.getId() + "-" + error.getSubId());
                rawIssue.setTool(getToolName());
                rawIssue.setDetail(error.getMsg());
                rawIssue.setFileName(relativePath);
                rawIssue.setCommitId(commit);
                if (!isTotalScan) {
                    rawIssue.setCommitId(file.substring(file.indexOf(commit) + 41, file.indexOf(commit) + 81));
                }
                rawIssue.setScanId(getToolName());
                rawIssue.setRepoUuid(repoUuid);
                rawIssue.setCodeLines(error.getLine());
                rawIssue.setLocations(parseLocations(error.getFile(), error.getLine(), error.getFuncInfo(), repoPath, commit, false, repoUuid));
                rawIssue.setDeveloperName(developerUniqueName);
                rawIssue.setPriority(CppIssuePriorityEnum.getRankByPriority(error.getSeverity()));
                String rawIssueUuid = RawIssue.generateRawIssueUUID(rawIssue);
                rawIssue.setUuid(rawIssueUuid);
                rawIssue.getLocations().forEach(location -> location.setRawIssueUuid(rawIssueUuid));
                rawIssue.setRawIssueHash(RawIssue.generateRawIssueHash(rawIssue));

                rawIssues.add(rawIssue);
            }

            resultRawIssues.addAll(rawIssues);
            log.info("raw issue number is {}", resultRawIssues.size());

        } catch (Exception e) {
            log.error("raw issue parse from xml error failed, msg: {}", e.getMessage());
            return false;
        }

        return true;
    }


    private FileNode getFileMetric(String filePath, String repoPrefix, String relativeFilePath, String nameWithNoSuffix, String commit) {
        boolean invoke = restInterfaceManager.invokeCppParser(repoPrefix, relativeFilePath, nameWithNoSuffix, commit);
        if (!invoke) {
            return null;
        }
        int count = 0;
        while (count < 10 && restInterfaceManager.getCppParseResult(nameWithNoSuffix, commit) == null){
            count++;
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                return null;
            }
        }
        JSONObject cppParseResult = restInterfaceManager.getCppParseResult(nameWithNoSuffix, commit);
        if (cppParseResult != null) {
            CppFileParser parser = new CppFileParser(filePath);
            parser.setFileNode(nameWithNoSuffix, relativeFilePath);
            try {
                parser.parseJson(cppParseResult);
                return parser.getFileNode();
            }catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private List<Location> parseLocations(String file, int line, String funcInfo, String repoPath, String commit, boolean cppParserSuccess, String repoUuid) {

        String code = AstUtil.getCode(line, line, file);
        String methodName = funcInfo.equals("test")?PARSE_METHOD_NAME_FAIL:funcInfo;

        if(cppParserSuccess){

            try{
                Map<String, int[]> methodInfoMap = method2Line.getOrDefault(file, new HashMap<>());

                List<String> possibleMethodNames = new ArrayList<>();

                for (Map.Entry<String, int[]> entry : methodInfoMap.entrySet()) {
                    if (entry.getValue()[0] <= line && entry.getValue()[1] >= line) {
                        possibleMethodNames.add(entry.getKey());
                        methodName = entry.getKey();
                    }
                }

                if (possibleMethodNames.size()>1){
                    String funcInfoNoSpace = funcInfo.replace(" ","");
                    Map<String,String> method2FullNameOfEachFile = method2FullName.getOrDefault(file,new HashMap<>());
                    for (String possibleMethodName : possibleMethodNames) {
                        String fullName = method2FullNameOfEachFile.getOrDefault(possibleMethodName,"");
                        String fullNameNoSpace = fullName.replace(" ","");
                        if(funcInfoNoSpace.equals(fullNameNoSpace)){
                            methodName = possibleMethodName;
                            break;
                        }
                    }
                }
            }catch (Exception ignore){

            }

        }

        String relativePath;
        if (isTotalScan) {
            relativePath = FileUtil.handleFileNameToRelativePath(file);
        } else {
            relativePath = file.substring(file.indexOf(commit) + 82);
        }

        Location location = Location.builder()
                .uuid(Location.generateLocationUUID(repoUuid,relativePath,line,line,0,0))
                .startLine(line)
                .endLine(line)
                .bugLines(line+"-"+line)
                .offset(0)
                .matchedIndex(-1)
                .matched(false)
                .filePath(relativePath)
                .locationMatchResults(new ArrayList<>())
                .anchorName(methodName)
                .code(code)
                .build();

        List<Location> locations = new ArrayList<>();
        locations.add(location);
        return locations;
    }

    @Override
    public String getToolName() {
        return ToolEnum.TSCANCODE.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        return null;
    }

    @Autowired
    public void setDeveloperUniqueNameUtil(DeveloperUniqueNameUtil developerUniqueNameUtil) {
        this.developerUniqueNameUtil = developerUniqueNameUtil;
    }

    @Autowired
    public void setIssueAnalyzerDao(IssueAnalyzerDao issueAnalyzerDao) {
        TscanCodeAnalyzer.issueAnalyzerDao = issueAnalyzerDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Override
    public boolean closeResourceLoader() {
        return false;
    }

    @Override
    public boolean fileFilter(String fileName){
        return FileFilter.cppFilenameFilter(fileName);
    }

}
