package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author beethoven
 */
@Repository
public class ProjectDao {

    private ProjectMapper projectMapper;

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public String getAllProjectIds() {
        List<String> projectIdsList = projectMapper.getAllProjectIds();
        StringBuilder sb = new StringBuilder();
        for (String s : projectIdsList) {
            sb.append(s).append(",");
        }
        return sb.substring(0, sb.toString().length() - 1);
    }

}
