-- MySQL dump 10.13  Distrib 5.1.71, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: neuroscience
-- ------------------------------------------------------
-- Server version 5.1.69

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
-- Table structure for table `Application`
--

DROP TABLE IF EXISTS `Application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Application` (
  `ApplicationID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Version` varchar(255) DEFAULT NULL,
  `ReleaseDate` varchar(255) DEFAULT NULL,
  `OS` varchar(255) DEFAULT NULL,
  `Platform` varchar(255) DEFAULT NULL,
  `Executable` varchar(255) DEFAULT NULL,
  `Type` int(11) DEFAULT '99',
  `Developers` varchar(255) DEFAULT NULL,
  `InternalName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ApplicationID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DataElement`
--

DROP TABLE IF EXISTS `DataElement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DataElement` (
  `DataID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) DEFAULT NULL,
  `ScanID` varchar(255) DEFAULT NULL,
  `URI` varchar(255) DEFAULT NULL,
  `Subject` varchar(255) DEFAULT NULL,
  `Type` varchar(255) DEFAULT NULL,
  `Format` varchar(255) DEFAULT NULL,
  `ResourceID` bigint(20) NOT NULL,
  `Date` datetime DEFAULT NULL,
  `Applications` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`DataID`),
  KEY `FKA8035BD2F614057C` (`ResourceID`),
  CONSTRAINT `FKA8035BD2F614057C` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=3920 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Error`
--

DROP TABLE IF EXISTS `Error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Error` (
  `ErrorID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Code` int(11) DEFAULT NULL,
  `Message` varchar(255) DEFAULT NULL,
  `Description` varchar(1024) DEFAULT NULL,
  `SubmissionID` bigint(20) NOT NULL,
  PRIMARY KEY (`ErrorID`),
  KEY `FK401E1E8FC083F38` (`SubmissionID`),
  CONSTRAINT `FK401E1E8FC083F38` FOREIGN KEY (`SubmissionID`) REFERENCES `Submission` (`SubmissionID`)
) ENGINE=InnoDB AUTO_INCREMENT=229 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IOBox`
--

DROP TABLE IF EXISTS `IOBox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IOBox` (
  `IOBoxID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Subject` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Action` varchar(255) DEFAULT NULL,
  `Sender` bigint(20) DEFAULT NULL,
  `Receiver` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IOBoxID`),
  KEY `FK429A2C5496AEC45` (`Sender`),
  KEY `FK429A2C54F36BBFF` (`Receiver`),
  CONSTRAINT `FK429A2C5496AEC45` FOREIGN KEY (`Sender`) REFERENCES `User` (`UserKey`),
  CONSTRAINT `FK429A2C54F36BBFF` FOREIGN KEY (`Receiver`) REFERENCES `User` (`UserKey`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IOBoxData`
--

DROP TABLE IF EXISTS `IOBoxData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IOBoxData` (
  `DataID` bigint(20) NOT NULL,
  `IOBoxID` bigint(20) NOT NULL,
  KEY `FKBD811C0FE2A6F400` (`IOBoxID`),
  KEY `FKBD811C0FD439E052` (`DataID`),
  CONSTRAINT `FKBD811C0FD439E052` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`),
  CONSTRAINT `FKBD811C0FE2A6F400` FOREIGN KEY (`IOBoxID`) REFERENCES `IOBox` (`IOBoxID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IOPort`
--

DROP TABLE IF EXISTS `IOPort`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IOPort` (
  `PortID` bigint(20) NOT NULL AUTO_INCREMENT,
  `PortNumber` int(11) DEFAULT NULL,
  `PortName` varchar(255) DEFAULT NULL,
  `IOType` varchar(255) DEFAULT NULL,
  `DataType` varchar(255) DEFAULT NULL,
  `DataFormat` varchar(255) DEFAULT NULL,
  `ApplicationID` bigint(20) NOT NULL,
  `ResourceID` bigint(20) NOT NULL,
  `Visible` tinyint(1) DEFAULT '1',
  `DisplayName` varchar(255) DEFAULT NULL,
  `OutputApps` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PortID`),
  KEY `FK811112C7FFCA75D6` (`ApplicationID`),
  KEY `FK811112C7F614057C` (`ResourceID`),
  CONSTRAINT `FK811112C7F614057C` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`),
  CONSTRAINT `FK811112C7FFCA75D6` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Preference`
--

DROP TABLE IF EXISTS `Preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Preference` (
  `PrefID` bigint(20) NOT NULL AUTO_INCREMENT,
  `PrefKey` varchar(255) DEFAULT NULL,
  `PrefValue` varchar(1024) DEFAULT NULL,
  `PrefDesc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PrefID`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Processing`
--

DROP TABLE IF EXISTS `Processing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Processing` (
  `ProcessingID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ProcessingName` varchar(255) DEFAULT NULL,
  `ProcessingDescription` varchar(255) DEFAULT NULL,
  `ProcessingDate` datetime DEFAULT NULL,
  `ProcessingDevelopers` varchar(255) DEFAULT NULL,
  `ProcessingStatus` varchar(255) DEFAULT NULL,
  `ApplicationID` bigint(20) NOT NULL,
  `ProjectID` bigint(20) NOT NULL,
  `Developers` varchar(255) DEFAULT NULL,
  `ProcessingLastUpdate` datetime DEFAULT NULL,
  PRIMARY KEY (`ProcessingID`),
  KEY `FK8FFC0B33FFCA75D6` (`ApplicationID`),
  KEY `FK8FFC0B3370D3F8A8` (`ProjectID`),
  CONSTRAINT `FK8FFC0B3370D3F8A8` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FK8FFC0B33FFCA75D6` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Project`
--

DROP TABLE IF EXISTS `Project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Project` (
  `ProjectID` bigint(20) NOT NULL AUTO_INCREMENT,
  `XnatID` varchar(255) DEFAULT NULL,
  `ProjectName` varchar(255) DEFAULT NULL,
  `ProjectDescription` varchar(512) DEFAULT NULL,
  `ProjectOwner` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectApplication`
--

DROP TABLE IF EXISTS `ProjectApplication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectApplication` (
  `ProjectID` bigint(20) NOT NULL,
  `ApplicationID` bigint(20) NOT NULL,
  KEY `FKF20D6497FFCA75D6` (`ApplicationID`),
  KEY `FKF20D649770D3F8A8` (`ProjectID`),
  CONSTRAINT `FKF20D649770D3F8A8` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FKF20D6497FFCA75D6` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectData`
--

DROP TABLE IF EXISTS `ProjectData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectData` (
  `ProjectID` bigint(20) NOT NULL,
  `DataID` bigint(20) NOT NULL,
  UNIQUE KEY `ProjectID` (`ProjectID`,`DataID`),
  KEY `FK2B5D224370D3F8A8` (`ProjectID`),
  KEY `FK2B5D2243D439E052` (`DataID`),
  CONSTRAINT `FK2B5D224370D3F8A8` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FK2B5D2243D439E052` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Property`
--

DROP TABLE IF EXISTS `Property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Property` (
  `PropertyID` bigint(20) NOT NULL AUTO_INCREMENT,
  `PropertyKey` varchar(255) DEFAULT NULL,
  `PropertyValue` varchar(1024) DEFAULT NULL,
  `PropertyDesc` varchar(255) DEFAULT NULL,
  `DCid` int(11) DEFAULT NULL,
  `DataID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PropertyID`),
  KEY `FKC8A841F5D439E052` (`DataID`),
  CONSTRAINT `FKC8A841F5D439E052` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Replica`
--

DROP TABLE IF EXISTS `Replica`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Replica` (
  `dbId` bigint(20) NOT NULL AUTO_INCREMENT,
  `ReplicaURI` varchar(255) DEFAULT NULL,
  `DataID` bigint(20) NOT NULL,
  `ResourceID` bigint(20) NOT NULL,
  PRIMARY KEY (`dbId`),
  KEY `FKA4756898F614057C` (`ResourceID`),
  KEY `FKA4756898D439E052` (`DataID`),
  CONSTRAINT `FKA4756898D439E052` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`),
  CONSTRAINT `FKA4756898F614057C` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Resource`
--

DROP TABLE IF EXISTS `Resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Resource` (
  `ResourceID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Storage` bit(1) DEFAULT NULL,
  `Computing` bit(1) DEFAULT NULL,
  PRIMARY KEY (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Status`
--

DROP TABLE IF EXISTS `Status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Status` (
  `dbId` bigint(20) NOT NULL AUTO_INCREMENT,
  `Value` varchar(255) DEFAULT NULL,
  `StatusTime` datetime DEFAULT NULL,
  `SubmissionID` bigint(20) NOT NULL,
  PRIMARY KEY (`dbId`),
  KEY `FK9432BC12FC083F38` (`SubmissionID`),
  CONSTRAINT `FK9432BC12FC083F38` FOREIGN KEY (`SubmissionID`) REFERENCES `Submission` (`SubmissionID`)
) ENGINE=InnoDB AUTO_INCREMENT=304 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Submission`
--

DROP TABLE IF EXISTS `Submission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Submission` (
  `SubmissionID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `Results` bit(1) DEFAULT NULL,
  `ProcessingID` bigint(20) NOT NULL,
  PRIMARY KEY (`SubmissionID`),
  KEY `FKFB08176CB8C25106` (`ProcessingID`),
  CONSTRAINT `FKFB08176CB8C25106` FOREIGN KEY (`ProcessingID`) REFERENCES `Processing` (`ProcessingID`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubmissionIO`
--

DROP TABLE IF EXISTS `SubmissionIO`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubmissionIO` (
  `dbId` bigint(20) NOT NULL AUTO_INCREMENT,
  `Type` varchar(255) DEFAULT NULL,
  `DataID` bigint(20) NOT NULL,
  `SubmissionID` bigint(20) NOT NULL,
  `PortID` bigint(20) NOT NULL,
  PRIMARY KEY (`dbId`),
  KEY `FK595FF592FEF01908` (`PortID`),
  KEY `FK595FF592FC083F38` (`SubmissionID`),
  KEY `FK595FF592D439E052` (`DataID`),
  CONSTRAINT `FK595FF592D439E052` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`),
  CONSTRAINT `FK595FF592FC083F38` FOREIGN KEY (`SubmissionID`) REFERENCES `Submission` (`SubmissionID`),
  CONSTRAINT `FK595FF592FEF01908` FOREIGN KEY (`PortID`) REFERENCES `IOPort` (`PortID`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `UserKey` bigint(20) NOT NULL AUTO_INCREMENT,
  `LiferayID` varchar(255) DEFAULT NULL,
  `UserID` varchar(255) DEFAULT NULL,
  `FirstName` varchar(255) DEFAULT NULL,
  `LastName` varchar(255) DEFAULT NULL,
  `Authentication` longblob,
  `Affiliation` varchar(255) DEFAULT NULL,
  `UserEmail` varchar(255) DEFAULT NULL,
  `Session` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`UserKey`),
  UNIQUE KEY `LiferayID` (`LiferayID`),
  UNIQUE KEY `UserID` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserActivity`
--

DROP TABLE IF EXISTS `UserActivity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivity` (
  `ActivityID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Activity` varchar(255) DEFAULT NULL,
  `Date` datetime DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `UserKey` bigint(20) NOT NULL,
  PRIMARY KEY (`ActivityID`),
  KEY `FK79519C1A76A5ECC4` (`UserKey`)
) ENGINE=MyISAM AUTO_INCREMENT=105 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserApplication`
--

DROP TABLE IF EXISTS `UserApplication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserApplication` (
  `ApplicationID` bigint(20) NOT NULL,
  `UserKey` bigint(20) NOT NULL,
  KEY `FK9356BC65FFCA75D6` (`ApplicationID`),
  KEY `FK9356BC6576A5ECC4` (`UserKey`),
  CONSTRAINT `FK9356BC6576A5ECC4` FOREIGN KEY (`UserKey`) REFERENCES `User` (`UserKey`),
  CONSTRAINT `FK9356BC65FFCA75D6` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserPreference`
--

DROP TABLE IF EXISTS `UserPreference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserPreference` (
  `UserKey` bigint(20) NOT NULL,
  `PrefID` bigint(20) NOT NULL,
  KEY `FK9A3C4F2676A5ECC4` (`UserKey`),
  KEY `FK9A3C4F2696CECDDE` (`PrefID`),
  CONSTRAINT `FK9A3C4F2676A5ECC4` FOREIGN KEY (`UserKey`) REFERENCES `User` (`UserKey`),
  CONSTRAINT `FK9A3C4F2696CECDDE` FOREIGN KEY (`PrefID`) REFERENCES `Preference` (`PrefID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserProcessing`
--

DROP TABLE IF EXISTS `UserProcessing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserProcessing` (
  `dbId` bigint(20) NOT NULL AUTO_INCREMENT,
  `ACL` bit(1) DEFAULT NULL,
  `UserKey` bigint(20) NOT NULL,
  `ProcessingID` bigint(20) NOT NULL,
  PRIMARY KEY (`dbId`),
  KEY `FKA69C15EB8C25106` (`ProcessingID`),
  KEY `FKA69C15E76A5ECC4` (`UserKey`),
  CONSTRAINT `FKA69C15E76A5ECC4` FOREIGN KEY (`UserKey`) REFERENCES `User` (`UserKey`),
  CONSTRAINT `FKA69C15EB8C25106` FOREIGN KEY (`ProcessingID`) REFERENCES `Processing` (`ProcessingID`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserProject`
--

DROP TABLE IF EXISTS `UserProject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserProject` (
  `ProjectID` bigint(20) NOT NULL,
  `UserKey` bigint(20) NOT NULL,
  UNIQUE KEY `UserKey` (`UserKey`,`ProjectID`),
  KEY `FK3EFBD4AE70D3F8A8` (`ProjectID`),
  KEY `FK3EFBD4AE76A5ECC4` (`UserKey`),
  CONSTRAINT `FK3EFBD4AE70D3F8A8` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FK3EFBD4AE76A5ECC4` FOREIGN KEY (`UserKey`) REFERENCES `User` (`UserKey`)
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

-- Dump completed on 2014-02-03 13:20:42
