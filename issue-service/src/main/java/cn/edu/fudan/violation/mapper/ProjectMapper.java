package cn.edu.fudan.violation.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Beethoven
 */
@Repository
public interface ProjectMapper {

    /**
     * 获取所有projectId
     *
     * @return String
     */
    List<String> getAllProjectIds();

}
