-- MySQL dump 10.13  Distrib 8.0.23, for osx10.16 (x86_64)
--
-- Host: 10.176.64.34    Database: issueTrackerTest
-- ------------------------------------------------------
-- Server version	5.7.20-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;
SET
@MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET
@@SESSION.SQL_LOG_BIN = 0;

--
-- GTID state at the beginning of the backup
--

SET
@@GLOBAL.GTID_PURGED = /*!80000 '+'*/ 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa:1-159780,
bac0d0b5-92dd-11e7-a6dd-74867af28ae8:1-57437195';

--
-- Table structure for table `ignore_record`
--

DROP TABLE IF EXISTS `ignore_record`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ignore_record`
(
    `uuid`         varchar(36) NOT NULL COMMENT '主键',
    `account_uuid` varchar(36)   DEFAULT NULL COMMENT '用户的ID',
    `account_name` varchar(36)   DEFAULT NULL COMMENT '用户名（聚合后的唯一名字）',
    `level`        int(11) DEFAULT NULL COMMENT 'ignore的级别：1代表只忽略一个issue（根据issueid）、2代表忽略本项目中此类issue、3代表忽略所有项目中的此类issue',
    `type`         varchar(128)  DEFAULT NULL COMMENT 'issue的类型',
    `tool`         varchar(45)   DEFAULT NULL COMMENT '缺陷检测工具',
    `repo_uuid`    varchar(36)   DEFAULT NULL COMMENT 'issue所属的repo的repo_id',
    `repo_name`    varchar(64)   DEFAULT NULL COMMENT 'issue所属repo的repo_name',
    `branch`       varchar(64)   DEFAULT NULL COMMENT 'issue所属repo的分支',
    `ignore_time`  datetime      DEFAULT NULL COMMENT '状态修改为ignore的时间',
    `issue_uuid`   varchar(36)   DEFAULT NULL COMMENT '要忽略的issue的issue_id',
    `raw_issue`    text COMMENT 'ide给的raw_issue',
    `commit_uuid`  varchar(128)  DEFAULT NULL COMMENT 'ide给的commit_id',
    `tag`          varchar(36) NOT NULL COMMENT '本次忽略要设置成的类型：Ignore;Misinformation;To review;Default(表示默认的状态，即不忽略)',
    `message`      varchar(1024) DEFAULT NULL,
    `file_path`    varchar(256)  DEFAULT NULL,
    `is_used`      int(11) DEFAULT '0',
    PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ignore_record`
--

LOCK
TABLES `ignore_record` WRITE;
/*!40000 ALTER TABLE `ignore_record`
    DISABLE KEYS */;
INSERT INTO `ignore_record` (`uuid`, `account_uuid`, `account_name`, `level`, `type`, `tool`, `repo_uuid`, `repo_name`,
                             `branch`, `ignore_time`, `issue_uuid`, `raw_issue`, `commit_uuid`, `tag`, `message`,
                             `file_path`, `is_used`)
VALUES ('625779a1-eee8-46be-895d-fe8b1c0ade20', NULL, 'shaoxi', 0,
        'Empty arrays and collections should be returned instead of null', 'sonarqube', NULL,
        'http://fdse.gitlab.com/platform/forTest', 'master', '2021-02-28 12:45:47', NULL,
        '{\"code_lines\":0,\"commit_id\":\"53d057f0419fb3dab5372a4f95c98d1d2fcb09aa\",\"detail\":\"Return an empty collection instead of null.\",\"locations\":[{\"bug_lines\":\"18\",\"code\":\"        return null;\",\"end_line\":18,\"end_token\":0,\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"method_name\":\"MethodNameChange2(int)\",\"offset\":1,\"start_line\":18,\"start_token\":0,\"tokens\":[-49,-55]}],\"mapped\":false,\"matchResultDTOIndex\":-1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"scan_id\":\"tempScan_id\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"tempuuid\"}',
        '53d057f0419fb3dab5372a4f95c98d1d2fcb09aa', 'Ignore', 'test0228',
        'src/main/java/application/issue/single/location/MethodName.java', 1),
       ('6e82bc6a-b713-4aef-a79b-de9809bcd4b1', NULL, 'shaoxi', 0,
        'Empty arrays and collections should be returned instead of null', 'sonarqube', NULL,
        'http://fdse.gitlab.com/platform/forTest', 'master', '2021-02-28 12:44:59', NULL,
        '{\"code_lines\":0,\"commit_id\":\"53d057f0419fb3dab5372a4f95c98d1d2fcb09aa\",\"detail\":\"Return an empty collection instead of null.\",\"locations\":[{\"bug_lines\":\"20\",\"code\":\"        return null;\",\"end_line\":20,\"end_token\":0,\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"method_name\":\"addLineIgnore()\",\"offset\":1,\"start_line\":20,\"start_token\":0,\"tokens\":[-49,-55]}],\"mapped\":false,\"matchResultDTOIndex\":-1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"scan_id\":\"tempScan_id\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"tempuuid\"}',
        '53d057f0419fb3dab5372a4f95c98d1d2fcb09aa', 'Ignore', 'test0228',
        'src/main/java/application/issue/single/location/AddLine.java', 1),
       ('baaae46e-f03c-4a56-82cf-9c338013ed91', NULL, 'shaoxi', 0,
        'Empty arrays and collections should be returned instead of null', 'sonarqube', NULL,
        'http://fdse.gitlab.com/platform/forTest', 'master', '2021-02-28 12:45:40', NULL,
        '{\"code_lines\":0,\"commit_id\":\"53d057f0419fb3dab5372a4f95c98d1d2fcb09aa\",\"detail\":\"Return an empty collection instead of null.\",\"locations\":[{\"bug_lines\":\"14\",\"code\":\"        return null;\",\"end_line\":14,\"end_token\":0,\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"method_name\":\"MethodNameChange2()\",\"offset\":1,\"start_line\":14,\"start_token\":0,\"tokens\":[-49,-55]}],\"mapped\":false,\"matchResultDTOIndex\":-1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"scan_id\":\"tempScan_id\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"tempuuid\"}',
        '53d057f0419fb3dab5372a4f95c98d1d2fcb09aa', 'Ignore', 'test0228',
        'src/main/java/application/issue/single/location/MethodName.java', 1);
/*!40000 ALTER TABLE `ignore_record`
    ENABLE KEYS */;
UNLOCK
TABLES;
