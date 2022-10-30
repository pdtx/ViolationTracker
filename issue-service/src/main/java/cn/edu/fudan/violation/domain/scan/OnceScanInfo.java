package cn.edu.fudan.violation.domain.scan;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单次 commit 扫描的信息
 *
 * @author fancying
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnceScanInfo implements Serializable, ScanStatus {

    @Deprecated
    private String uuid;

    @Deprecated
    private int totalCommitCount;

    @Deprecated
    private int scannedCommitCount;

    @Deprecated
    private String latestCommit;


    private int id;

    LocalDateTime startScanTime;
    LocalDateTime endScanTime;
    private String scanStatus;


    /**
     * 总耗时 单位s
     */
    private long scanTime;
    private String branch;
    private String repoUuid;
    private String commitHash;
    private String tool;
    private String commitTime;

    public static OnceScanInfo initializeOnceScanInfo(String repoUuid, String branch, String commitHash, String tool, String commitTime) {
        return OnceScanInfo.builder().
                commitHash(commitHash).
                startScanTime(LocalDateTime.now()).
                endScanTime(LocalDateTime.now()).
                scanTime(0).
                scanStatus(OnceScanInfo.SCANNING).
                branch(branch).
                tool(tool).
                commitTime(commitTime).
                repoUuid(repoUuid).build();
    }


    /**
     * 描述节点的状态
     */
    public enum Status {

        /**
         * scanning
         */
        SCANNING("scanning"),

        COMPLETE("complete"),
        FAILED("failed");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

}
