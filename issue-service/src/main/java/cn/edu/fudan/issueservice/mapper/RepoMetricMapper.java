package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.RepoMetric;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author beethoven
 * @date 2021-06-28 09:26:39
 */
@Repository
public interface RepoMetricMapper {

    /**
     * get repo metric
     *
     * @param repoUuids
     * @return
     */
    List<RepoMetric> getDeveloperLivingIssueLevel(List<String> repoUuids);
}
