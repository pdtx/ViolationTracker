package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.mapper.SolvedRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @description:
 * @author: keyon
 * @time: 2021/11/18 8:36 下午
 */
@Repository
public class SolvedRecordDao {
    private SolvedRecordMapper solvedRecordMapper;

    @Autowired
    public void setSolvedRecordMapper(SolvedRecordMapper solvedRecordMapper) {
        this.solvedRecordMapper = solvedRecordMapper;
    }

    public String getTypeByIssueUuid(String issueUuid) {
        return solvedRecordMapper.getTypeByIssueId(issueUuid);
    }
}
