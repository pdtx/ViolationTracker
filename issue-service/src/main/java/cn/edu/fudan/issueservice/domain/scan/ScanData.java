package cn.edu.fudan.issueservice.domain.scan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description: 存储需要扫描的项目信息
 *
 * @author fancying
 * create: 2021-03-01 21:54
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanData {
    String repoUuid;
    String branch;
    String repoPath;
    boolean initialScan;
    List<String> toScanCommitList;

    RepoScan repoScan;
    Integer scannedCommitCount;
}
