package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueAnalyzer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author Jeff
 */
@Repository
public interface IssueAnalyzerMapper {

    /**
     * 插入issueIgnore记录
     *
     * @param issueAnalyzer issueAnalyzer
     */
    void insertIssueAnalyzerRecords(IssueAnalyzer issueAnalyzer);


    /**
     * 尝试获取解析结果
     *
     * @param repoUuid repoUuid
     * @param commitId commitId
     * @param tool     工具名
     * @return IssueAnalyzer
     */
    IssueAnalyzer getIssueAnalyzeResultByRepoUuidCommitIdTool(@Param("repoUuid") String repoUuid,
                                                              @Param("commitId") String commitId,
                                                              @Param("tool") String tool);

    /**
     * 查询是否有缓存
     *
     * @param repoUuid repoUuid
     * @param commitId commitId
     * @param tool     工具名
     * @return 是否有缓存
     */
    @Select(
            "        SELECT count(*)\n"
                    + "        FROM raw_issue_cache\n"
                    + "        WHERE repo_uuid = #{repoUuid}\n"
                    + "          AND commit_id = #{commitId}\n"
                    + "          AND tool = #{tool}")
    Integer cached(String repoUuid, String commitId, String tool);

    Integer getOneCommitTotalIssueNum(@Param("repoUuid") String repoUuid, @Param("commitId") String commitId);

    void updateTotalIssueNum(@Param("repoUuid") String repoUuid, @Param("commitId") String commitId, @Param("num") Integer num);

    Integer getInvokeResult(@Param("repoUuid") String repoUuid,
                            @Param("commitId") String commitId,
                            @Param("tool") String tool);
}
