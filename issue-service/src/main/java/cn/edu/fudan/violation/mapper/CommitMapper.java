package cn.edu.fudan.violation.mapper;

import cn.edu.fudan.violation.annotation.MapF2F;
import cn.edu.fudan.violation.domain.dbo.Commit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public interface CommitMapper {

    /**
     * 获取commit列表
     *
     * @param repoId          repoUuid
     * @param startCommitTime 时间
     * @return commit list
     */
    List<Commit> getCommits(@Param("repo_uuid") String repoId, @Param("start_commit_time") String startCommitTime);

    /**
     * 根据commitId 获取commitView 的相关信息
     *
     * @param repoId   repoUuid
     * @param commitId commitId
     * @return ommitView信息
     */
    Map<String, Object> getCommitViewInfoByCommitId(@Param("repo_uuid") String repoId, @Param("commit_id") String commitId);

    /**
     * 根据commitId获取commit信息
     *
     * @param repoUuid    repoUuid
     * @param startCommit commit
     * @return commit信息
     */
    Commit getCommitByCommitId(String repoUuid, String startCommit);

    /**
     * get commit time by commit id
     *
     * @param commit   commit id
     * @param repoUuid repoUuid
     * @return commit time
     */
    String getCommitTimeByCommitId(String commit, String repoUuid);

    /**
     * get parent commit
     *
     * @param commitTime commitTime
     * @param repoUuid   repoUuid
     * @return
     */
    List<Map<String, Object>> getParentCommits(String commitTime, String repoUuid);

    /**
     * get commit message
     *
     * @param commitId commitId
     * @param repoUuid repoUuid
     * @return commit message
     */
    @Select("SELECT message FROM commit WHERE commit_id = #{commitId} AND repo_uuid = #{repoUuid} limit 1")
    String getCommitMessageByCommitIdAndRepoUuid(String commitId, String repoUuid);

    /**
     * get repo count
     *
     * @param developers developers
     * @param since      since
     * @param until      until
     * @param repoUuids  repoUuids
     * @return
     */
    @MapF2F
    Map<String, String> getRepoCountByDeveloper(List<String> developers, String since, String until, List<String> repoUuids);

    /**
     * 获取commits
     * @param repoUuid
     * @param developer
     * @param since
     * @param until
     * @return
     */
    List<Commit> getCommitsInDeveloperAndPeriod(@Param("repoUuid") String repoUuid, @Param("developer") String developer, @Param("since") String since, @Param("until") String until);
}
