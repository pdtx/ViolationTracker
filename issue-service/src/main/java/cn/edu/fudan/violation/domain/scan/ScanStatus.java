package cn.edu.fudan.violation.domain.scan;

/**
 * description: 扫描的三种状态  正在扫描、完成、失败
 *
 * @author fancying
 * create: 2021-02-24 15:35
 **/
public interface ScanStatus {

    /**
     * 库扫描的状态
     */
    String SCANNING = "scanning";
    String COMPLETE = "complete";
    String FAILED = "failed";
    String STOP = "stop";
    String WAITING_FOR_SCAN = "waiting for scan";

    /**
     * 单次扫描的状态
     */
    String DOING = "doing";
    String CHECKOUT_FAILED = "checkout failed";
    String COMPILE_FAILED = "compile failed";
    String INVOKE_TOOL_FAILED = "invoke tool failed";
    String ANALYZE_FAILED = "analyze failed";
    String PERSIST_FAILED ="persist failed";
    String MATCH_FAILED = "match failed";
    String STATISTICAL_FAILED = "statistical failed";
    String DONE = "done";
    String NOT_CHANGE = "not change";
}
