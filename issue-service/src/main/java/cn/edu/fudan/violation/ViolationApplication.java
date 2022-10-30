package cn.edu.fudan.violation;

import cn.edu.fudan.violation.core.IssueScanProcess;
import lombok.NonNull;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author Beethoven
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan("cn.edu.fudan.violation.mapper")
@EnableTransactionManagement
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@EnableCaching(proxyTargetClass = true)
@EnableSwagger2
public class ViolationApplication implements ApplicationRunner, ApplicationContextAware {

    ApplicationContext applicationContext;



    public static void main(String[] args) {
        SpringApplication.run(ViolationApplication.class, args);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {


    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
