-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: 10.135.132.106    Database: issueTrackerTest
-- ------------------------------------------------------
-- Server version	5.7.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

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
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2021-05-26 17:31:00
