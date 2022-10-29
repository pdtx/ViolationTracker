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
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `location`
(
    `uuid`          varchar(36)  NOT NULL COMMENT '主键',
    `start_line`    mediumint(9) DEFAULT NULL COMMENT 'bug所在上下文的开始行',
    `end_line`      mediumint(9) DEFAULT NULL COMMENT 'bug所在上下文的结尾行',
    `bug_lines`     varchar(4096) DEFAULT NULL COMMENT '表示这个bug在文件中具体体现在哪些行',
    `start_token`   mediumint(9) DEFAULT NULL,
    `end_token`     mediumint(9) DEFAULT NULL,
    `file_name`     varchar(512) NOT NULL COMMENT 'bug所在文件路径',
    `class_name`    varchar(256)  DEFAULT NULL COMMENT 'bug所在类名',
    `method_name`   text COMMENT 'bug所在方法名',
    `rawIssue_uuid` varchar(36)  NOT NULL COMMENT 'bug所属rawissueID',
    `code`          text COMMENT 'bug源代码',
    PRIMARY KEY (`uuid`) USING BTREE,
    UNIQUE KEY `uuid` (`uuid`) USING BTREE,
    KEY             `raw_issue_id_index` (`rawIssue_uuid`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK
TABLES `location` WRITE;
/*!40000 ALTER TABLE `location`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `location`
    ENABLE KEYS */;
UNLOCK
TABLES;
