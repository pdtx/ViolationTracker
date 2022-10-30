package cn.edu.fudan.violation.mapper;

import cn.edu.fudan.violation.domain.dbo.IssueType;
import cn.edu.fudan.violation.domain.dbo.IssueTypeJSON;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Beethoven
 */
@Repository
public interface IssueTypeMapper {

    /**
     * 获取issueType
     *
     * @param type type
     * @return issueType
     */
    IssueType getIssueTypeByTypeName(@Param("type") String type);

    void insertIssueTypes(@Param("issueTypes") List<IssueTypeJSON> issueTypes);

    List<IssueTypeJSON> getIssueTypeJsons();
}
