package cn.edu.fudan.issueservice;

import cn.edu.fudan.issueservice.core.solved.IssueSolved;
import lombok.NonNull;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
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
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;


/**
 * @author Beethoven
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan("cn.edu.fudan.issueservice.mapper")
@EnableTransactionManagement
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@EnableCaching(proxyTargetClass = true)
@EnableSwagger2
public class IssueServiceApplication implements ApplicationRunner, ApplicationContextAware {

    ApplicationContext applicationContext;

//    @Autowired
//    IssueSolved issueSolved;

    public static void main(String[] args) {
        SpringApplication.run(IssueServiceApplication.class, args);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

//        String cimRepoUuid = "ece2e322-9b69-11ec-8596-35aebb3ef88e";
//        issueSolved.updateSolvedWay(Collections.singletonList(cimRepoUuid));

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
