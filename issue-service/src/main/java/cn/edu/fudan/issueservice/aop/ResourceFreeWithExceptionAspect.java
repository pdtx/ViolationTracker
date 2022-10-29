package cn.edu.fudan.issueservice.aop;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ResourceFreeWithExceptionAspect {

    private RestInterfaceManager restInvoker;

    // 定义切点

    @Pointcut("@annotation(cn.edu.fudan.issueservice.annotation.FreeResourceWithException)")
    public void repoSourceManager() {
    }

    // 定义执行操作
    @Around("repoSourceManager()") //around 与 下面参数名around对应
    public void controlRepoSource(ProceedingJoinPoint point) throws Throwable {
        log.info("ANNOTATION 调用类：" + point.getSignature().getDeclaringTypeName());
        log.info("ANNOTATION 调用类名" + point.getSignature().getDeclaringType().getSimpleName());
        for (Object o : point.getArgs()) {
            if (o instanceof RepoResourceDTO) {
                RepoResourceDTO repoResourceDTO = (RepoResourceDTO) o;
                String repoPath = restInvoker.getCodeServiceRepo(repoResourceDTO.getRepoUuid());
                repoResourceDTO.setRepoPath(repoPath);
                log.info("get repo:{}, path:{}", repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                try {
                    point.proceed();
                } finally {
                    log.info("free repo:{}, path:{}", repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                    restInvoker.freeRepoPath(repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                }
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
