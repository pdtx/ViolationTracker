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
bac0d0b5-92dd-11e7-a6dd-74867af28ae8:1-57409231';

--
-- Table structure for table `rawIssue`
--

DROP TABLE IF EXISTS `rawIssue`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rawIssue`
(
    `uuid`       varchar(36)  NOT NULL COMMENT '主键',
    `type`       varchar(200) NOT NULL COMMENT '缺陷类型',
    `tool`       varchar(45)  DEFAULT NULL COMMENT 'rawissue类别',
    `detail`     mediumtext,
    `file_name`  varchar(512) DEFAULT NULL COMMENT 'rawissue文件名',
    `scan_id`    varchar(36)  NOT NULL COMMENT 'rawissue扫描id',
    `issue_id`   varchar(36)  DEFAULT NULL,
    `commit_id`  varchar(64)  NOT NULL COMMENT '本次commit id',
    `repo_id`    varchar(36)  NOT NULL COMMENT 'rawissue所属仓库id',
    `code_lines` int(11) DEFAULT NULL,
    `status`     varchar(36)  DEFAULT 'default',
    `developer`  varchar(64)  DEFAULT NULL,
    PRIMARY KEY (`uuid`) USING BTREE,
    UNIQUE KEY `uuid` (`uuid`) USING BTREE,
    KEY          `idx_rawIssue_repo_id` (`repo_id`) USING BTREE,
    KEY          `idx_rawIssue_issue_id` (`issue_id`) USING BTREE,
    KEY          `idx_rawIssue_category` (`tool`) USING BTREE,
    KEY          `idx_rawIssue_status` (`status`) USING BTREE,
    KEY          `idx_issue_commit_status` (`issue_id`, `commit_id`, `status`) USING BTREE,
    KEY          `idx_uuid_commit_status` (`uuid`, `tool`, `repo_id`) USING BTREE,
    KEY          `idx_category_repoId` (`tool`, `repo_id`) USING BTREE,
    KEY          `idx_rawIssue_repo_id_category` (`repo_id`, `tool`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
