package cn.edu.fudan.issueservice.domain.scan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * description: 项目的整体扫描信息
 *  每完成一次 commit list扫描 记录一次信息
 *
 * @author fancying
 * create: 2021-02-24 15:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoScan implements ScanStatus {

    int id;

    String repoUuid;
    String branch;

    String tool;
    String scanStatus;

    int totalCommitCount;
    int scannedCommitCount;

    LocalDateTime startScanTime;
    LocalDateTime endScanTime;

    boolean initialScan;
    String startCommit;
    String endCommit;

    long scanTime;

}
