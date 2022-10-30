package cn.edu.fudan.violation.annotation;

import java.lang.annotation.*;

/**
 * 定义日志访问注解 实现资源的释放 方法参数中必须要有 RepoResourceDTO
 *
 * @author fancying
 * create: 2020-04-22 10:20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FreeResource {

}
