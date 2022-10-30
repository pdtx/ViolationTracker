package cn.edu.fudan.violation.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author beethoven
 * @date 2021-07-01 15:08:22
 */
@Slf4j
public class ShUtil {

    public static boolean executeCommand(String command, int sleepTime) {
        try {
            Runtime rt = Runtime.getRuntime();
            log.info("command -> {}", command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(sleepTime, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! ({}s)", sleepTime);
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            log.error("command execute error, command is: {}", command);
            log.error("exception msg is: {}", e.getMessage());
        }
        return false;
    }
}
