package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.domain.dbo.RepoMetric;
import cn.edu.fudan.violation.mapper.RepoMetricMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 * @date 2021-06-28 09:26:39
 */
@Repository
public class RepoMetricDao {

    private RepoMetricMapper repoMetricMapper;

    @Autowired
    public void setRepoMetricMapper(RepoMetricMapper repoMetricMapper) {
        this.repoMetricMapper = repoMetricMapper;
    }


    public Map<String, List<int[]>> getDeveloperLivingIssueLevel(List<String> repoUuids) {

        List<RepoMetric> repoMetricList = repoMetricMapper.getDeveloperLivingIssueLevel(repoUuids);
        if (repoMetricList == null) {
            repoMetricList = repoMetricMapper.getDeveloperLivingIssueLevel(null);
        }

        Map<String, RepoMetric> repoMetrics = new HashMap<>(repoMetricList.size() * 2);
        repoMetricList.forEach(repoMetric -> repoMetrics.put(repoMetric.getRepoUuid(), repoMetric));

        Map<String, List<int[]>> result = new HashMap<>(16);
        for (String repoUuid : repoUuids) {
            List<int[]> ints = new ArrayList<>();
            RepoMetric repoMetric = repoMetrics.getOrDefault(repoUuid, new RepoMetric());
            ints.add(new int[]{repoMetric.getWorstMax(), repoMetric.getWorstMin(), 1});
            ints.add(new int[]{repoMetric.getWorseMax(), repoMetric.getWorseMin(), 2});
            ints.add(new int[]{repoMetric.getNormalMax(), repoMetric.getNormalMin(), 3});
            ints.add(new int[]{repoMetric.getBetterMax(), repoMetric.getBetterMin(), 4});
            ints.add(new int[]{repoMetric.getBestMax(), repoMetric.getBestMin(), 5});
            result.put(repoUuid, ints);
        }

        return result;
    }
}
