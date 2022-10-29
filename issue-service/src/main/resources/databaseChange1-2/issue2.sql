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
-- Table structure for table `issue`
--

DROP TABLE IF EXISTS `issue`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `issue`
(
    `id`                int(11) NOT NULL AUTO_INCREMENT,
    `type`              varchar(128) DEFAULT NULL,
    `tool`              varchar(45)  DEFAULT NULL,
    `start_commit`      varchar(64)  DEFAULT NULL,
    `start_commit_date` datetime     DEFAULT NULL,
    `end_commit`        varchar(64)  DEFAULT NULL,
    `end_commit_date`   datetime     DEFAULT NULL,
    `repo_uuid`         varchar(36)  DEFAULT NULL,
    `file_name`         varchar(512) DEFAULT NULL,
    `create_time`       datetime     DEFAULT NULL,
    `update_time`       datetime     DEFAULT NULL,
    `priority`          tinyint(4) DEFAULT NULL,
    `status`            varchar(20)  DEFAULT NULL,
    `manual_status`     varchar(45)  DEFAULT 'Default',
    `resolution`        varchar(20)  DEFAULT NULL,
    `issue_category`    varchar(50)  DEFAULT NULL,
    `producer`          varchar(64)  DEFAULT NULL,
    `solver`            varchar(64)  DEFAULT NULL,
    `solve_commit`      varchar(64)  DEFAULT NULL,
    `solve_commit_date` datetime     DEFAULT NULL,
    `uuid`              varchar(36)  DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `issue`
--

LOCK
TABLES `issue` WRITE;
/*!40000 ALTER TABLE `issue`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `issue`
    ENABLE KEYS */;
UNLOCK
TABLES;
