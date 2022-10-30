package cn.edu.fudan.violation.aop;

import cn.edu.fudan.violation.component.RestInterfaceManager;
import cn.edu.fudan.violation.domain.dto.RepoResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author fancying
 * create: 2020-04-22 16:02
 **/
@Aspect
@Component
@Slf4j
public class ResourceAspect {

    private RestInterfaceManager restInvoker;

    // 定义切点

    @Pointcut("@annotation(cn.edu.fudan.violation.annotation.FreeResource)")
    public void release() {
    }

    // 定义执行操作

    @AfterReturning("release()")
    public void releaseRepoRelease(JoinPoint joinPoint) {
        for (Object o : joinPoint.getArgs()) {
            if (o instanceof RepoResourceDTO) {
                RepoResourceDTO repoResourceDTO = (RepoResourceDTO) o;
                log.info("free repo:{}, path:{}", repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                restInvoker.freeRepoPath(repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
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