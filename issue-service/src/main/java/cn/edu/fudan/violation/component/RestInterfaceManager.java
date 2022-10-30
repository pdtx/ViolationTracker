package cn.edu.fudan.violation.component;

import cn.edu.fudan.violation.config.RestTemplateConfig;
import cn.edu.fudan.violation.domain.enums.ToolEnum;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author fancying
 * @author zjf
 * @author heyue
 * @author WZY
 * @version 1.0
 **/
@Component
@Slf4j
@Import(RestTemplateConfig.class)
public class RestInterfaceManager  {

    private static final String REPO_NAME = "repoName";
    private static final String TOKEN = "token";
    private static final String PROJECT_URL = "/repo?repo_uuid=";


    public static final String DATA = "data";
    @Value("${sonar.login}")
    public String sonarLogin;
    @Value("${sonar.password}")
    public String sonarPassword;
    @Value("${repoPrefix}")
    String repoPrefix;

    private String projectServicePath;

    private String scanServicePath;
    @Value("${cppParser.service.path}")
    private String cppParserServicePath;
    @Value("${sonar.service.path}")
    private String sonarServicePath;
    @Value("${enable.target.repo.path}")
    private boolean enableTargetRepoPath;
    private boolean initSonarAuth = false;
    private HttpEntity<HttpHeaders> sonarAuthHeader;
    @Value("${debugMode}")
    private boolean debugMode;
    @Value("${language}")
    private String language;

    @Autowired
    private RestTemplate restTemplate;



















    //todo:
    public String[] getToolsByRepoUuid(String repoUuid) {
        if(debugMode || (language == null)){
            return new String[]{"sonarqube"};
        }
        return new String[]{"sonarqube"};


    }

    public String getToolByRepoUuid(String repoUuid) {
        JSONObject repoInfo = restTemplate.getForObject(projectServicePath + PROJECT_URL + repoUuid, JSONObject.class);
        assert repoInfo != null;
        assert repoInfo.getJSONObject(DATA) != null;
        String language = repoInfo.getJSONObject(DATA).getString("language");
        return ToolEnum.getToolByLanguage(language);
    }

    public JSONObject getTagDetail(String tagId, String userToken){
        Map<String,String> map = new HashMap<>(16);
        String urlBuilder = projectServicePath + "/tag/detail/{tagId}";
        map.put("tagId",String.valueOf(tagId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(TOKEN, userToken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(httpHeaders);
        try{
            ResponseEntity<JSONObject> entity = restTemplate.exchange(urlBuilder, HttpMethod.GET, request, JSONObject.class, map);
            return JSONObject.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }





    //---------------------------------------------code service---------------------------------------------------------

    public String getCodeServiceRepo(String repoId) {
        if (enableTargetRepoPath) {
            return repoPrefix + repoId;
        }

        return null;
    }

    public void freeRepoPath(String repoId, String repoPath) {
        if (enableTargetRepoPath) {
            return;
        }
        return;
    }

    //--------------------------------------------------------sonar api -----------------------------------------------------

    private void initSonarAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        String encoding = DatatypeConverter.printBase64Binary((sonarLogin + ":" + sonarPassword).getBytes(StandardCharsets.UTF_8));
        headers.add("Authorization", "Basic " + encoding);
        this.sonarAuthHeader = new HttpEntity<>(headers);
        initSonarAuth = true;
    }

    public JSONArray getSonarIssueDirectories(String componentKeys) {
        if (!initSonarAuth) {
            initSonarAuthorization();
        }
        String url = sonarServicePath + "/api/issues/search?componentKeys={componentKeys}&ps=1&p=1&facets=directories";
        Map<String, String> map = new HashMap<>();
        map.put("componentKeys", componentKeys);
        try {
            ResponseEntity<JSONObject> entity = restTemplate.exchange(url, HttpMethod.POST, sonarAuthHeader, JSONObject.class, map);
            JSONObject sonarResult = JSONObject.parseObject(Objects.requireNonNull(entity.getBody()).toString());
            JSONArray facets = sonarResult.getJSONArray("facets");
            for (Object o1 : facets) {
                if ("directories".equals(((JSONObject) o1).getString("property"))) {
                    return ((JSONObject) o1).getJSONArray("values");
                }
            }
            return null;
        } catch (RuntimeException e) {
            log.error("repo name : {}  ----> request sonar api failed getSonarIssueDirectories", componentKeys);
            return null;
        }
    }

    public JSONObject getSonarIssueFileUuidsInDirectory(String componentKey, String directory) {
        if (!initSonarAuth) {
            initSonarAuthorization();
        }
        String url = sonarServicePath + "/api/issues/search?componentKeys={componentKeys}&directories={directories}&ps=1&p=1&facets=files";
        Map<String, String> map = new HashMap<>();
        map.put("componentKeys", componentKey);
        map.put("directories", directory);
        try {
            ResponseEntity<JSONObject> entity = restTemplate.exchange(url, HttpMethod.POST, sonarAuthHeader, JSONObject.class, map);
            return JSON.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        } catch (RuntimeException e) {
            log.error("repo name : {}  ----> request sonar api failed getSonarIssueFileUuidsInDirectory", componentKey);
            return null;
        }
    }

    public JSONObject getSonarSecurityHotspot(String hotspot) {
        if (!initSonarAuth) {
            initSonarAuthorization();
        }
        String url = sonarServicePath + "/api/hotspots/show?hotspot={hotspot}";
        Map<String, String> map = new HashMap<>();
        map.put("hotspot", hotspot);
        try {
            ResponseEntity<JSONObject> entity = restTemplate.exchange(url, HttpMethod.GET, sonarAuthHeader, JSONObject.class, map);
            return JSON.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        } catch (RuntimeException e) {
            log.error("repo name : {}  ----> request sonar api failed getSonarSecurityHotspot", hotspot);
            return null;
        }
    }


    public JSONObject getSonarSecurityHotspotList(String repoName, int pageSize, int page) {

        if(!initSonarAuth) {
            initSonarAuthorization();
        }
        StringBuilder urlBuilder = new StringBuilder(sonarServicePath + "/api/hotspots/search?projectKey={repoName}");
        Map<String,String> map = new HashMap<>(4);
        map.put(REPO_NAME, repoName);
        if (page > 0) {
            urlBuilder.append("&p={p}");
            map.put("p", String.valueOf(page));
        }
        if (pageSize > 0) {
            urlBuilder.append("&ps={ps}");
            map.put("ps", String.valueOf(pageSize));
        }
        try {
            ResponseEntity<JSONObject> entity = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, sonarAuthHeader, JSONObject.class, map);
            return JSON.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        } catch (RuntimeException e) {
            log.error("repo name : {}  ----> request sonar api failed getSonarSecurityHotspotList", repoName);
            return null;
        }

    }

    public JSONObject getSonarIssueResults(String repoName, String directories, String fileUuids, String type, int pageSize, boolean resolved, int page) {

        if (!initSonarAuth) {
            initSonarAuthorization();
        }

        Map<String, String> map = new HashMap<>(16);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(sonarServicePath).append("/api/issues/search?componentKeys={componentKeys}&additionalFields={additionalFields}&s={s}&resolved={resolved}");
        map.put("additionalFields", "_all");
        map.put("s", "FILE_LINE");
        map.put("componentKeys", repoName);
        map.put("resolved", String.valueOf(resolved));
        if (!StringUtils.isEmpty(directories)) {
            urlBuilder.append("&directories={directories}");
            map.put("directories", directories);
        }
        if (!StringUtils.isEmpty(fileUuids)) {
            urlBuilder.append("&files={files}");
            map.put("files", fileUuids);
        }
        if (type != null) {
            String[] types = type.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for (String typeSb : types) {
                if ("CODE_SMELL".equals(typeSb) || "BUG".equals(typeSb) || "VULNERABILITY".equals(typeSb) || "SECURITY_HOTSPOT".equals(typeSb)) {
                    stringBuilder.append(typeSb).append(",");
                }
            }
            if (!stringBuilder.toString().isEmpty()) {
                urlBuilder.append("&types={types}");
                String requestTypes = stringBuilder.substring(0, stringBuilder.toString().length() - 1);
                map.put("types", requestTypes);
            } else {
                log.error("this request type --> {} is not available in sonar api", type);
                return null;
            }
        }

        if (page > 0) {
            urlBuilder.append("&p={p}");
            map.put("p", String.valueOf(page));
        }
        if (pageSize > 0) {
            urlBuilder.append("&ps={ps}");
            map.put("ps", String.valueOf(pageSize));
        }

        String url = urlBuilder.toString();

        try {
            ResponseEntity<JSONObject> entity = restTemplate.exchange(url, HttpMethod.GET, sonarAuthHeader, JSONObject.class, map);
            return JSONObject.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        } catch (RuntimeException e) {
            log.error("repo name : {}  ----> request sonar api failed getSonarIssueResults", repoName);
            log.debug("error page is {}", page);
            return null;
        }
    }

    public JSONObject getRuleInfo(String ruleKey, String actives, String organizationKey) {

        if (!initSonarAuth) {
            initSonarAuthorization();
        }

        Map<String, String> map = new HashMap<>(64);

        String baseRequestUrl = sonarServicePath + "/api/rules/show";
        if (ruleKey == null) {
            log.error("ruleKey is missing");
            return null;
        } else {
            map.put("key", ruleKey);
        }
        if (actives != null) {
            map.put("actives", actives);
        }
        if (organizationKey != null) {
            map.put("organization", organizationKey);
        }

        try {
            return restTemplate.exchange(baseRequestUrl + "?key=" + ruleKey, HttpMethod.GET, sonarAuthHeader, JSONObject.class).getBody();
        } catch (Exception e) {
            log.error("ruleKey : {}  ----> request sonar  rule information api failed", ruleKey);
            return null;
        }

    }

    public JSONObject getSonarAnalysisTime(String projectName) {

        if (!initSonarAuth) {
            initSonarAuthorization();
        }

        JSONObject error = new JSONObject();
        error.put("errors", "Component key " + projectName + " not found");

        try {
            String urlPath = sonarServicePath + "/api/components/show?component=" + projectName;
            log.debug(urlPath);
            return restTemplate.exchange(urlPath, HttpMethod.GET, sonarAuthHeader, JSONObject.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("projectName: {} ---> request sonar api failed 获取最新版本时间API 失败", projectName);
        }

        return error;
    }




    public boolean invokeCppParser(String prefix, String relativeFilePath, String fileName, String commit) {
        JSONObject param = new JSONObject();
        param.put("repoPath", prefix);
        param.put("filePath", relativeFilePath);
        param.put("filename", fileName);
        param.put("commit", commit);
        try {
            String invokeParserPath = cppParserServicePath + "/cpp-parser/start";
            JSONObject requestResult = restTemplate.postForObject(invokeParserPath, param, JSONObject.class);
            if (requestResult != null) {
                int code = requestResult.getInteger("code");
                if (code == HttpStatus.OK.value()) {
                    return true;
                }
            }
        } catch (Exception var8) {
            return false;
        }
        return false;
    }

    public JSONObject getCppParseResult(String fileName, String commit) {
        StringBuilder url = new StringBuilder();
        String resultParserPath = cppParserServicePath + "/cpp-parser/result";
        url.append(resultParserPath).append("?filename=").append(fileName);
        url.append(resultParserPath).append("?commit=").append(commit);
        try {
            JSONObject result = restTemplate.getForObject(url.toString(), JSONObject.class);
            if (result != null) {
                return result.getJSONObject("content");
            }
        } catch (Exception var5) {
//            log.error(var5.getMessage());
        }
        return null;
    }





}
