package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.mapper.RepoMeasureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author Joshua
 * @description
 * @date 2022-07-27 16:25
 **/
@Repository
public class RepoMeasureDao {

    private RepoMeasureMapper repoMeasureMapper;

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper){
        this.repoMeasureMapper = repoMeasureMapper;
    }

    public int getAbsoluteLinesByRepoUuid(String repoUuid){
        Integer absoluteLinesByRepoId = repoMeasureMapper.getAbsoluteLinesByRepoId(repoUuid);
        return Objects.requireNonNullElse(absoluteLinesByRepoId, 0);
    }

}
