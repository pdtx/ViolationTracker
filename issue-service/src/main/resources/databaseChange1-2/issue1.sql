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
-- Table structure for table `issue`
--

DROP TABLE IF EXISTS `issue`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `issue`
(
    `uuid`              varchar(36)  NOT NULL COMMENT '主键',
    `type`              varchar(128) NOT NULL COMMENT '缺陷类型',
    `tool`              varchar(45)           DEFAULT NULL COMMENT '缺陷检测工具',
    `start_commit`      varchar(64)           DEFAULT NULL COMMENT '缺陷出现的那次commit',
    `start_commit_date` datetime              DEFAULT NULL COMMENT '缺陷出现的那次commit的时间',
    `end_commit`        varchar(64)           DEFAULT NULL COMMENT '缺陷结束的那次commit',
    `end_commit_date`   datetime              DEFAULT NULL COMMENT '缺陷结束的那次commit的时间',
    `repo_id`           varchar(36)           DEFAULT NULL COMMENT '所属repo',
    `target_files`      varchar(512)          DEFAULT NULL COMMENT '所在文件',
    `create_time`       datetime              DEFAULT NULL COMMENT 'issue被创建时的时间',
    `update_time`       datetime              DEFAULT NULL COMMENT 'issue最近一次更新的时间',
    `priority`          tinyint(4) DEFAULT NULL COMMENT 'issue的优先级（紧急程度）',
    `display_id`        int(11) DEFAULT NULL COMMENT '在前端展示的id',
    `status`            varchar(20)           DEFAULT NULL COMMENT 'issue的状态',
    `manual_status`     varchar(45)           DEFAULT 'Default' COMMENT '记录用户手动更改缺陷状态的信息',
    `resolution`        varchar(20)           DEFAULT NULL COMMENT '如果status的状态为Solved，则该字段加1表示该缺陷被解决的次数',
    `issue_category`    varchar(50)           DEFAULT NULL COMMENT '缺陷的分类：如bug，code smell ',
    `producer`          varchar(64)  NOT NULL DEFAULT 'Default' COMMENT '首次引入缺陷的开发者(不受编译失败影响)',
    `solver`            varchar(64)           DEFAULT NULL,
    PRIMARY KEY (`uuid`) USING BTREE,
    UNIQUE KEY `uuid` (`uuid`) USING BTREE,
    KEY                 `category` (`tool`) USING BTREE,
    KEY                 `repo_id` (`repo_id`) USING BTREE,
    KEY                 `repo_id_and_priority` (`repo_id`, `priority`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC;
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
