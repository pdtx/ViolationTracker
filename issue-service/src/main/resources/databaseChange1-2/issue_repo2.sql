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
bac0d0b5-92dd-11e7-a6dd-74867af28ae8:1-57408827';

--
-- Table structure for table `issue_repo`
--

DROP TABLE IF EXISTS `issue_repo`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `issue_repo`
(
    `uuid`                 varchar(36) COLLATE utf8mb4_bin NOT NULL,
    `repo_uuid`            varchar(36) COLLATE utf8mb4_bin NOT NULL,
    `branch`               varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
    `tool`                 varchar(45) COLLATE utf8mb4_bin  DEFAULT NULL,
    `start_commit`         varchar(64) COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '开始扫描的commit id',
    `end_commit`           varchar(64) COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '最后一次扫描的commit id',
    `total_commit_count`   int(10) DEFAULT NULL COMMENT '需要扫描的总 commit 数量',
    `scanned_commit_count` int(10) DEFAULT NULL COMMENT '已扫描的commit 数量',
    `scan_time`            int(10) DEFAULT NULL COMMENT '扫描时间，以秒为单位',
    `status`               varchar(45) COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '扫描状态',
    `nature`               varchar(45) COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '是第一次扫描，还是更新扫描',
    `start_scan_time`      datetime                         DEFAULT NULL COMMENT '开始扫描的时间',
    `end_scan_time`        datetime                         DEFAULT NULL COMMENT '最后一次扫描的时间',
    PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `issue_repo`
--

LOCK
TABLES `issue_repo` WRITE;
/*!40000 ALTER TABLE `issue_repo`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `issue_repo`
    ENABLE KEYS */;
UNLOCK
TABLES;
