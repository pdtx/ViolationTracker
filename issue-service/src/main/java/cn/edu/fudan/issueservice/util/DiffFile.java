package cn.edu.fudan.issueservice.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * description: 文件都是linux格式的相对路径
 *
 * @author fancying
 * create: 2021/11/15
 **/
@Getter
public class DiffFile {
    /**
     * todo rename addRelativeFilePaths
     */
    List<String> addFiles;
    List<String> deleteFiles;
    /**
     * key old
     * value new
     */
    Map<String, String> changeFiles;

    public DiffFile(List<String> addFiles, List<String> deleteFiles, Map<String, String> changeFiles) {
        this.addFiles = ImmutableList.copyOf(addFiles);
        this.deleteFiles = ImmutableList.copyOf(deleteFiles);
        this.changeFiles = ImmutableMap.copyOf(changeFiles);
    }

}
