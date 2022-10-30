package cn.edu.fudan.violation.util;

import cn.edu.fudan.violation.dao.CommitDao;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author beethoven
 * @date 2021-08-12 12:27:58
 */
@Component
public class DeveloperUniqueNameUtil {

    private CommitDao commitDao;

    @SneakyThrows
    public String getDeveloperUniqueName(String repoPath, String commit, String repoUuid) {
        JGitHelper jGitInvoker = new JGitHelper(repoPath);
        String developerUniqueName = jGitInvoker.getAuthorName(commit);
//        Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoUuid, commit);
//        if (commitViewInfo != null) {
//            developerUniqueName = commitViewInfo.get("developer_unique_name") == null ? developerUniqueName : (String) commitViewInfo.get("developer_unique_name");
//        }
        jGitInvoker.close();
        return developerUniqueName;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }
}
