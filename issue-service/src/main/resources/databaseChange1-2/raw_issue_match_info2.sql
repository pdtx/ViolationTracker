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
-- Table structure for table `raw_issue_match_info`
--

DROP TABLE IF EXISTS `raw_issue_match_info`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `raw_issue_match_info`
(
    `id`                int(11) NOT NULL AUTO_INCREMENT,
    `cur_rawIssue_uuid` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
    `cur_commit_id`     varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
    `pre_rawIssue_uuid` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
    `pre_commit_id`     varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
    `issue_uuid`        varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
    `status`            varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 65
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `raw_issue_match_info`
--

LOCK
TABLES `raw_issue_match_info` WRITE;
/*!40000 ALTER TABLE `raw_issue_match_info`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `raw_issue_match_info`
    ENABLE KEYS */;
UNLOCK
TABLES;
