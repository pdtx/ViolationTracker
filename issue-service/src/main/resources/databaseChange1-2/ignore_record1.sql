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
    PRIMARY KEY (`uuid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
