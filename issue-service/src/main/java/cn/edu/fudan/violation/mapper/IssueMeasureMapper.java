package cn.edu.fudan.violation.mapper;

import cn.edu.fudan.violation.domain.dbo.Violation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description
 *
 * @Copyright DoughIt Studio - Powered By DoughIt
 * @author Jerry Zhang <https://github.com/doughit>
 * @date 2022-08-05 09:48
 */
@Repository
public interface IssueMeasureMapper {
    void addOneViolation(@Param("violation") Violation violation);

    void addViolations(@Param("violations") List<Violation> violations);
}
