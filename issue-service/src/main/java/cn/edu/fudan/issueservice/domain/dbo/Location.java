package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dto.LocationMatchResult;
import cn.edu.fudan.issueservice.util.CosineUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * fixme 修改不符合规范的field命名 已修改
 *
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location {

    /**
     * location表主键是id，uuid为非唯一索引
     * uuid生成策略 文件名、startLine、endLine、startToken、endToken、repoUuid
     * uuid 用于生成rawIssueUuid、benchmark数据校验
     */
    private String uuid;
    private int startLine;
    private int endLine;
    @Deprecated
    private String bugLines;
    private int startToken;
    private int endToken;
    private String filePath;
    private String className;
    /**
     * 父结点名称 如果存在于方法类，则不变 如果是成员变量，则返回类名
     */
    private String anchorName;
    private String rawIssueUuid;
    /**
     * 选取startLine endLine startToken endToken 指定的逻辑行代码
     */
    private String code;

    private String repoUuid;

    /**
     * location 起始位置相对于 所在方法或者属性起始位置的偏移量
     */
    private int offset = 0;

    private List<LocationMatchResult> locationMatchResults = new ArrayList<>(0);
    private boolean matched = false;
    private int matchedIndex = -1;

    private List<Byte> tokens = null;

    /**
     * 扫描时采用的路径，仅便于解析，不入库
     */
    private String scanFilePath;

//    public static List<Location> valueOf(JSONArray locations) {
//        List<Location> locationList = new ArrayList<>();
//        for (int i = 0; i < locations.size(); i++) {
//            JSONObject tempLocation = locations.getJSONObject(i);
//            Location location = new Location();
//            location.setUuid(UUID.randomUUID().toString());
//            location.setBugLines(tempLocation.getString("bug_lines"));
//            location.setCode(tempLocation.getString("code"));
//            location.setStartLine(tempLocation.getIntValue("start_line"));
//            location.setEndLine(tempLocation.getIntValue("end_line"));
//            location.setMethodName(tempLocation.getString("method_name"));
//            locationList.add(location);
//        }
//        return locationList;
//    }

    public static String generateLocationUUID(String repoUuid, String filePath, int startLine, int endLine, int startToken, int endToken){
        String locationString = repoUuid + "_" +
                filePath + "_" +
                startLine + "_" +
                endLine + "_" +
                startToken + "_" +
                endToken;
        return UUID.nameUUIDFromBytes(locationString.getBytes()).toString();
    }

    public String generateLocationUUID(){
        String locationString = repoUuid + "_" +
                filePath + "_" +
                startLine + "_" +
                endLine + "_" +
                startToken + "_" +
                endToken;
        return UUID.nameUUIDFromBytes(locationString.getBytes()).toString();
    }

    public String generateLocationUUIDDebug(){
        String locationString = repoUuid + "_" +
                filePath + "_" +
                startLine + "_" +
                endLine;
        return UUID.nameUUIDFromBytes(locationString.getBytes()).toString();
    }

    public List<Byte> getTokens() {
        if (tokens == null) {

            // 去掉注释的token
            tokens = CosineUtil.lexer(CosineUtil.removeComment(code), true);
        }
        return tokens;
    }

    public boolean isSame(Location location) {
        if (StringUtils.isEmpty(anchorName) || StringUtils.isEmpty(code) ||
                StringUtils.isEmpty(location.getAnchorName()) || StringUtils.isEmpty(location.getCode())) {
            return false;
        }

        return anchorName.equals(location.getAnchorName()) && code.equals(location.getCode());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location location = (Location) obj;
        if (this.className != null && location.className != null
                && this.anchorName != null && location.anchorName != null) {
            if (bugLines == null && location.bugLines == null) {
                return location.className.equals(className) &&
                        location.anchorName.equals(anchorName) &&
                        location.filePath.equals(filePath);
            } else if (bugLines != null && location.bugLines != null) {

                return location.className.equals(className) &&
                        location.anchorName.equals(anchorName) &&
                        location.filePath.equals(filePath) &&
                        bugLines.split(",").length == location.bugLines.split(",").length;

            }

        }
        return false;
    }

    public void setMappedLocation(Location location2, double matchDegree) {
        matched = true;
        locationMatchResults.add(LocationMatchResult.newInstance(location2, matchDegree));
    }
}
