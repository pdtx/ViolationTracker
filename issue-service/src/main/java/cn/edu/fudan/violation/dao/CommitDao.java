package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.domain.dbo.Commit;
import cn.edu.fudan.violation.mapper.CommitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 */
@Repository
public class CommitDao {

    private CommitMapper commitMapper;

    @Autowired
    public void setCommitViewMapper(CommitMapper commitMapper) {
        this.commitMapper = commitMapper;
    }






    public String getCommitMessageByCommitIdAndRepoUuid(String commitId, String repoUuid) {
        return commitMapper.getCommitMessageByCommitIdAndRepoUuid(commitId, repoUuid);
    }

    public Map<String, String> getRepoCountByDeveloper(List<String> developers, String since, String until, List<String> repoUuids) {
        return commitMapper.getRepoCountByDeveloper(developers, since, until, repoUuids);
    }

    public List<Commit> getCommitsInDeveloperAndPeriod(String repoUuid, String developer, String since, String until) {
        return commitMapper.getCommitsInDeveloperAndPeriod(repoUuid, developer, since, until);
    }
}
