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
bac0d0b5-92dd-11e7-a6dd-74867af28ae8:1-57408828';

--
-- Table structure for table `scan_result`
--

DROP TABLE IF EXISTS `scan_result`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scan_result`
(
    `id`               int(11) NOT NULL AUTO_INCREMENT COMMENT '主键\r\n',
    `category`         varchar(45) NOT NULL COMMENT '扫描结果类型',
    `repo_uuid`        varchar(36) NOT NULL COMMENT '扫描的repo文件id',
    `scan_date`        date        NOT NULL COMMENT '扫描日期',
    `commit_id`        varchar(64) NOT NULL COMMENT '本次commit id',
    `commit_date`      date        NOT NULL COMMENT '本次commit时间',
    `developer`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本次commit的提交者',
    `new_count`        int(11) DEFAULT '0' COMMENT '新增缺陷总数',
    `eliminated_count` int(11) DEFAULT '0' COMMENT '消除缺陷总数',
    `remaining_count`  int(11) DEFAULT '0' COMMENT '剩余缺陷总数 ',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 25973
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scan_result`
--

LOCK
TABLES `scan_result` WRITE;
/*!40000 ALTER TABLE `scan_result`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `scan_result`
    ENABLE KEYS */;
UNLOCK
TABLES;
