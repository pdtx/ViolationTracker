-- MySQL dump 10.13  Distrib 8.0.23, for osx10.16 (x86_64)
--
-- Host: 10.176.64.34    Database: issueTracker
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
bac0d0b5-92dd-11e7-a6dd-74867af28ae8:1-57408831';

--
-- Table structure for table `issue_scan`
--

DROP TABLE IF EXISTS `issue_scan`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `issue_scan`
(
    `uuid`           varchar(36) NOT NULL COMMENT '主键',
    `tool`           varchar(45) DEFAULT NULL COMMENT '扫描工具类型',
    `start_time`     datetime    DEFAULT NULL COMMENT '扫描开始时间',
    `end_time`       datetime    DEFAULT NULL COMMENT '扫描结束时间',
    `status`         varchar(32) NOT NULL COMMENT '扫描完成状态',
    `result_summary` mediumtext COMMENT '扫描结果内容总结',
    `repo_id`        varchar(36) DEFAULT NULL COMMENT '项目仓库id',
    `commit_id`      varchar(64) DEFAULT NULL COMMENT '本次commit id',
    `commit_time`    datetime    DEFAULT NULL COMMENT '本次commit时间',
    PRIMARY KEY (`uuid`) USING BTREE,
    UNIQUE KEY `uuid` (`uuid`) USING BTREE,
    KEY              `idx_scan_repo_id` (`repo_id`) USING BTREE,
    KEY              `idx_scan_tool` (`tool`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `issue_scan`
--

LOCK
TABLES `issue_scan` WRITE;
/*!40000 ALTER TABLE `issue_scan`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `issue_scan`
    ENABLE KEYS */;
UNLOCK
TABLES;
