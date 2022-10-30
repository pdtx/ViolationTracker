package cn.edu.fudan.violation.aop;

import cn.edu.fudan.violation.component.RestInterfaceManager;
import cn.edu.fudan.violation.domain.dto.RepoResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-25 09:31
 **/
@Aspect
@Component
@Slf4j
public class ResourceGetAspect {

    private RestInterfaceManager restInvoker;

    @Pointcut("@annotation(cn.edu.fudan.violation.annotation.GetResource)")
    public void getResource() {
    }

    @Before("getResource()")
    public void getRepoResource(JoinPoint joinPoint) {
        for (Object o : joinPoint.getArgs()) {
            if (o instanceof RepoResourceDTO) {
                RepoResourceDTO repoResourceDTO = (RepoResourceDTO) o;
                String repoPath = restInvoker.getCodeServiceRepo(repoResourceDTO.getRepoUuid());
                repoResourceDTO.setRepoPath(repoPath);
                log.info("get repo:{}, path:{}", repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                return;
            }
        }
        log.error("no parameter RepoResourceDTO ");
    }

    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }

}