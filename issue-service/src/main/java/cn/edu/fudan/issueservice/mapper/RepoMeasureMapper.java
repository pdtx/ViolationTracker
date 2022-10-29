package cn.edu.fudan.issueservice.mapper;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Joshua
 * @description
 * @date 2022-07-27 16:26
 **/
@Repository
public interface RepoMeasureMapper {

    Integer getAbsoluteLinesByRepoId(@Param("repoUuid") String repoUuid);

}
