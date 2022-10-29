package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.core.IssueScanProcess;
import cn.edu.fudan.issueservice.core.solved.IssueSolved;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.dto.ScanRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


/**
 * description: issue 工具调用
 *
 * @author fancying
 * create: 2020-05-19 21:03
 **/
@Api(value = "issue scan", tags = {"用于控制issue扫描的相关接口"})
@Slf4j
@RestController
public class IssueScanController {

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed ";
    private static final String INVOKE_TOOL_FAILED_MESSAGE = "invoke tool:[{}] failed! message is {}";
    private ApplicationContext applicationContext;
    private RestInterfaceManager restInterfaceManager;
    private IssueSolved issueSolved;

    @PostMapping(value = {"/issue/scan"})
    public ResponseBean<String> scanStart(@RequestBody ScanRequestDTO scanRequestDTO) {
        String repoUuid = scanRequestDTO.getRepoUuid();
        String branch = scanRequestDTO.getBranch();
        String beginCommit = scanRequestDTO.getBeginCommit();
        String endCommit = scanRequestDTO.getEndCommit();
        try {
            IssueScanProcess issueScanProcess = applicationContext.getBean(IssueScanProcess.class);
            issueScanProcess.setFirstScan(!StringUtils.isEmpty(beginCommit));
            issueScanProcess.scan(repoUuid, branch, beginCommit, endCommit);
            return new ResponseBean<>(200, "success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, "invoke tool failed!", e.getMessage());
        }
    }

    @PutMapping(value = {"/issue/update/solve-way"})
    public ResponseBean<String> updateSolveWay(@RequestParam("repo_uuid") String repoUuid) {
        try {
            issueSolved.updateSolvedWay(Arrays.asList(repoUuid.split(",")));
            return new ResponseBean<>(200, "success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, "update repo failed!", e.getMessage());
        }
    }


    @ApiOperation(value = "根据工具和repoId停止相应的扫描", notes = "@return String", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", required = true)
    })
    @GetMapping(value = {"/issue/scan-stop"})
    public ResponseBean<String> stopScan(@RequestParam("repo_uuid") String repoUuid, @RequestParam("tool") String tool) {
//        String tool = restInterfaceManager.getToolByRepoUuid(repoUuid);
        if (tool == null) {
            return new ResponseBean<>(400, FAILED, "stop failed!");
        }

        try {
            IssueScanProcess issueScanProcess = applicationContext.getBean(IssueScanProcess.class);
            issueScanProcess.stopScan(repoUuid, tool);
            return new ResponseBean<>(200, SUCCESS, "stop success!");
        } catch (Exception e) {
            log.error(INVOKE_TOOL_FAILED_MESSAGE, tool, e.getMessage());
            return new ResponseBean<>(500, FAILED, e.getMessage());
        }
    }





    @Autowired
    public void setIssueSolved(IssueSolved issueSolved) {
        this.issueSolved = issueSolved;
    }


    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
