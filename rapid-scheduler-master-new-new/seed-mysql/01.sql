CREATE DATABASE  IF NOT EXISTS `DS` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `DS`;
-- MySQL dump 10.13  Distrib 5.7.9, for osx10.9 (x86_64)
--
-- Host: localhost    Database: DS
-- ------------------------------------------------------
-- Server version	5.7.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_info` (
  `userid` int(20) NOT NULL AUTO_INCREMENT,
  `ipv4` varchar(15) DEFAULT NULL,
  `qosparam` varchar(100) DEFAULT NULL,
  `deadline` timestamp NULL DEFAULT NULL,
  `cycles` bigint(11) DEFAULT NULL,
  `vcpu` int(20) DEFAULT NULL,
  `memory` int(20) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vm_info`
--

--
-- Table structure for table `vmm_info`
--

DROP TABLE IF EXISTS `vmm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vmm_info` (
  `vmmid` int(20) NOT NULL AUTO_INCREMENT,
  `ipv4` varchar(15) DEFAULT NULL,
  `mactype` int(11) DEFAULT NULL,
  `macaddress` varchar(30) DEFAULT NULL,
  `suspended` tinyint(1) DEFAULT 0,
  `cpuload` int(11) DEFAULT NULL,
  `allocatedcpu` float DEFAULT 0.0,
  `cpufrequency` int(11) DEFAULT NULL,
  `cpunums` int(11) DEFAULT NULL,
  `freemem` int(11) DEFAULT NULL,
  `availmem` int(11) DEFAULT NULL,
  `powerusage` int(11) DEFAULT NULL,
  `freegpu` int(11) DEFAULT NULL,
  `gpunums` int(11) DEFAULT NULL,
  `availtypes` varchar(60) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`vmmid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

DROP TABLE IF EXISTS `vm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vm_info` (
   `vmid` int(20) NOT NULL AUTO_INCREMENT,
   `ipv4` varchar(15) DEFAULT NULL,
   `port` int(11) DEFAULT NULL,
   `vmmid` int(20) DEFAULT NULL,
   `category` int(11) DEFAULT NULL,
   `type` int(11) DEFAULT NULL,
   `userid` int(20) DEFAULT NULL,
   `offloadstatus` int(11) DEFAULT NULL,
   `vmstatus` int(11) DEFAULT NULL,
   `vcpu` int(11) DEFAULT NULL,
   `memory` int(11) DEFAULT NULL,
   `timestamp` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`vmid`),
   CONSTRAINT FK_VMINFO_VMM FOREIGN KEY (`vmmid`) REFERENCES `vmm_info`(`vmmid`),
   CONSTRAINT FK_VMINFO_USER FOREIGN KEY (`userid`) REFERENCES `user_info`(`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `vmm_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vmm_stats` (
    `statid` int(20) NOT NULL AUTO_INCREMENT,
    `vmmid` int(20) NOT NULL,
    `cpuload` int(11) DEFAULT NULL,
    `allocatedcpu` float DEFAULT NULL,
    `freemem` int(11) DEFAULT NULL,
    `availmem` int(11) DEFAULT NULL,
    `powerusage` int(11) DEFAULT NULL,
    `timestamp` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`statid`),
    CONSTRAINT FK_STATS_VMM FOREIGN KEY (`vmmid`) REFERENCES `vmm_info`(`vmmid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `offload_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `offload_history` (
    `offloadid` int(20) NOT NULL AUTO_INCREMENT,
    `vmmid` int(11) NOT NULL,
    `userid` int(11) NOT NULL,
    `deadline` timestamp NULL DEFAULT NULL,
    `vcpu` int(11) NOT NULL,
    `memory` int(11) NOT NULL,
    `cycles` bigint(11) NOT NULL,
    `timestamp` timestamp NULL DEFAULT NULL,
    `finished_timestamp` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`offloadid`),
    CONSTRAINT FK_OFFLOAD_VMM FOREIGN KEY (`vmmid`) REFERENCES `vmm_info`(`vmmid`),
    CONSTRAINT FK_OFFLOAD_USER FOREIGN KEY (`userid`) REFERENCES `user_info`(`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `request_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_info` (
   `requestid` int(20) NOT NULL AUTO_INCREMENT,
   `accepted` tinyint(1) DEFAULT NULL,
   `vmmid` int(11) DEFAULT NULL,
   `userid` int(11) DEFAULT NULL,
   `deadline` timestamp NULL DEFAULT NULL,
   `vcpu` int(11) NOT NULL,
   `memory` int(11) NOT NULL,
   `cycles` bigint(11) NOT NULL,
   `timestamp` timestamp NULL DEFAULT NULL,
   `startQueueTime` timestamp NULL DEFAULT NULL, -- Added column for queue time
   `endQueueTime` timestamp NULL DEFAULT NULL, -- Added column for queue time
   PRIMARY KEY (`requestid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `wol_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wol_history` (
   `wolid` int(20) NOT NULL AUTO_INCREMENT,
   `vmmid` int(11) NOT NULL,
   `iswol` tinyint(1) NOT NULL,
   `timestamp` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`wolid`),
   CONSTRAINT FK_WOL_VMM FOREIGN KEY (`vmmid`) REFERENCES `vmm_info`(`vmmid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `globalreadings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `globalreadings` (
   `glid` int(20) NOT NULL AUTO_INCREMENT,
   `activevmms` int(11) DEFAULT 0,
   `allocatedcpu` float DEFAULT 0.0,
   `powerusagesum` int(11) DEFAULT 0,
   `powerusageavg` float DEFAULT 0,
   'turnaroundSum' int(11) DEFAULT 0,
   'turnaroundAvg' float DEFAULT 0,
   `timestamp` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`glid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
