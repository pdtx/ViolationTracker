package cn.edu.fudan.violation.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Joshua
 * @description
 * @date 2022-03-04 19:48
 **/
@Repository
public interface AccountMapper {

    /**
     * 修改被合并人关联信息
     *
     * @param majorAccountName 主合并人姓名
     * @return name
     */
    List<String> getGitnameByAccountName(@Param("majorAccountName") String majorAccountName);

}
