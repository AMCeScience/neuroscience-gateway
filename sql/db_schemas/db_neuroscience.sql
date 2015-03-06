-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: neuroscience
-- ------------------------------------------------------
-- Server version	5.1.73

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
  `Description` varchar(255) DEFAULT NULL,
  `Executable` varchar(255) DEFAULT NULL,
  `InternalName` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Platform` varchar(255) DEFAULT NULL,
  `Type` int(11) DEFAULT NULL,
  `Version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ApplicationID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DataElement`
--

DROP TABLE IF EXISTS `DataElement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DataElement` (
  `DataID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Date` datetime DEFAULT NULL,
  `Existing` bit(1) DEFAULT NULL,
  `Format` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Size` int(11) DEFAULT NULL,
  `Type` varchar(255) DEFAULT NULL,
  `URI` varchar(2048) DEFAULT NULL,
  `ResourceID` bigint(20) NOT NULL,
  PRIMARY KEY (`DataID`),
  KEY `FK_1qwogc2txsp0st949oat459lj` (`ResourceID`),
  CONSTRAINT `FK_1qwogc2txsp0st949oat459lj` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=27307 DEFAULT CHARSET=latin1;
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
  `Description` varchar(1024) DEFAULT NULL,
  `Message` varchar(255) DEFAULT NULL,
  `SubmissionID` bigint(20) NOT NULL,
  PRIMARY KEY (`ErrorID`),
  KEY `FK_29cems25iqg15j0e9kr1vj26k` (`SubmissionID`),
  CONSTRAINT `FK_29cems25iqg15j0e9kr1vj26k` FOREIGN KEY (`SubmissionID`) REFERENCES `Submission` (`SubmissionID`)
) ENGINE=InnoDB AUTO_INCREMENT=42451 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IOPort`
--

DROP TABLE IF EXISTS `IOPort`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IOPort` (
  `PortID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DataFormat` varchar(255) DEFAULT NULL,
  `DataType` varchar(255) DEFAULT NULL,
  `DisplayName` varchar(255) DEFAULT NULL,
  `IOType` varchar(255) DEFAULT NULL,
  `PortName` varchar(255) DEFAULT NULL,
  `PortNumber` int(11) DEFAULT NULL,
  `Visible` bit(1) DEFAULT NULL,
  `ApplicationID` bigint(20) NOT NULL,
  `ChainedInputPortID` bigint(20) DEFAULT NULL,
  `ResourceID` bigint(20) NOT NULL,
  PRIMARY KEY (`PortID`),
  KEY `FK_nr4vmwfd8x1qvloudo88215aw` (`ApplicationID`),
  KEY `FK_40yhp0tiw5taoo915cf042hom` (`ChainedInputPortID`),
  KEY `FK_no9swrqn8vjlsb361loefagb1` (`ResourceID`),
  CONSTRAINT `FK_40yhp0tiw5taoo915cf042hom` FOREIGN KEY (`ChainedInputPortID`) REFERENCES `IOPort` (`PortID`),
  CONSTRAINT `FK_no9swrqn8vjlsb361loefagb1` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`),
  CONSTRAINT `FK_nr4vmwfd8x1qvloudo88215aw` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `KeyName`
--

DROP TABLE IF EXISTS `KeyName`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KeyName` (
  `KeyID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`KeyID`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Preference`
--

DROP TABLE IF EXISTS `Preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Preference` (
  `PrefID` bigint(20) NOT NULL AUTO_INCREMENT,
  `PrefDesc` varchar(255) DEFAULT NULL,
  `PrefKey` varchar(255) DEFAULT NULL,
  `PrefValue` varchar(1024) DEFAULT NULL,
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
  `ProcessingDate` datetime DEFAULT NULL,
  `ProcessingDescription` varchar(255) DEFAULT NULL,
  `ProcessingName` varchar(255) DEFAULT NULL,
  `ApplicationID` bigint(20) NOT NULL,
  `ProjectID` bigint(20) NOT NULL,
  `UserID` bigint(20) NOT NULL,
  PRIMARY KEY (`ProcessingID`),
  KEY `FK_9bwpjdr6u0otb8rvjwte68ibx` (`ApplicationID`),
  KEY `FK_due7ryw3v8wylqi69qagbvjmj` (`ProjectID`),
  KEY `FK_fbuwavhvbt5l2rdo9oqbstuyk` (`UserID`),
  CONSTRAINT `FK_9bwpjdr6u0otb8rvjwte68ibx` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`),
  CONSTRAINT `FK_due7ryw3v8wylqi69qagbvjmj` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FK_fbuwavhvbt5l2rdo9oqbstuyk` FOREIGN KEY (`UserID`) REFERENCES `User` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=492 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Project`
--

DROP TABLE IF EXISTS `Project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Project` (
  `ProjectID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ProjectDescription` varchar(512) DEFAULT NULL,
  `ProjectName` varchar(255) DEFAULT NULL,
  `Owner` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectApplication`
--

DROP TABLE IF EXISTS `ProjectApplication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectApplication` (
  `ApplicationID` bigint(20) NOT NULL,
  `ProjectID` bigint(20) NOT NULL,
  KEY `FK_sf1wuy2k7xby00lj6m6p5rdcu` (`ProjectID`),
  KEY `FK_fnbxpwog15re5kg2cyrj8o99r` (`ApplicationID`),
  CONSTRAINT `FK_fnbxpwog15re5kg2cyrj8o99r` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`),
  CONSTRAINT `FK_sf1wuy2k7xby00lj6m6p5rdcu` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectData`
--

DROP TABLE IF EXISTS `ProjectData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectData` (
  `DataID` bigint(20) NOT NULL,
  `ProjectID` bigint(20) NOT NULL,
  KEY `FK_qmewuc37q8wp0paa0qsr21n44` (`ProjectID`),
  KEY `FK_5xsp4b5m6flgculu43jg7pbvx` (`DataID`),
  CONSTRAINT `FK_5xsp4b5m6flgculu43jg7pbvx` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`),
  CONSTRAINT `FK_qmewuc37q8wp0paa0qsr21n44` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Replica`
--

DROP TABLE IF EXISTS `Replica`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Replica` (
  `ReplicaID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ReplicaURI` varchar(255) DEFAULT NULL,
  `DataID` bigint(20) NOT NULL,
  `ResourceID` bigint(20) NOT NULL,
  PRIMARY KEY (`ReplicaID`),
  KEY `FK_l6scv164tnlpytjg8l6ea7opc` (`DataID`),
  KEY `FK_lryycy4m7jfbh5w9igbx0yih` (`ResourceID`),
  CONSTRAINT `FK_l6scv164tnlpytjg8l6ea7opc` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`),
  CONSTRAINT `FK_lryycy4m7jfbh5w9igbx0yih` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=8763 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Resource`
--

DROP TABLE IF EXISTS `Resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Resource` (
  `ResourceID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BaseURI` varchar(255) DEFAULT NULL,
  `Computing` bit(1) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Protocol` varchar(255) DEFAULT NULL,
  `Robot` bit(1) DEFAULT NULL,
  `Storage` bit(1) DEFAULT NULL,
  PRIMARY KEY (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Status`
--

DROP TABLE IF EXISTS `Status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Status` (
  `StatusID` bigint(20) NOT NULL AUTO_INCREMENT,
  `StatusTime` datetime DEFAULT NULL,
  `Value` varchar(255) DEFAULT NULL,
  `SubmissionID` bigint(20) NOT NULL,
  PRIMARY KEY (`StatusID`),
  KEY `FK_au53i4ej2fubi5puah3ibmv9y` (`SubmissionID`),
  CONSTRAINT `FK_au53i4ej2fubi5puah3ibmv9y` FOREIGN KEY (`SubmissionID`) REFERENCES `Submission` (`SubmissionID`)
) ENGINE=InnoDB AUTO_INCREMENT=47663 DEFAULT CHARSET=latin1;
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
  `Results` bit(1) DEFAULT NULL,
  `ProcessingID` bigint(20) NOT NULL,
  PRIMARY KEY (`SubmissionID`),
  KEY `FK_srqxw9uuaofr34yjnghprcgs7` (`ProcessingID`),
  CONSTRAINT `FK_srqxw9uuaofr34yjnghprcgs7` FOREIGN KEY (`ProcessingID`) REFERENCES `Processing` (`ProcessingID`)
) ENGINE=InnoDB AUTO_INCREMENT=6132 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubmissionIO`
--

DROP TABLE IF EXISTS `SubmissionIO`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubmissionIO` (
  `SubmissionIOID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Type` varchar(255) DEFAULT NULL,
  `DataID` bigint(20) NOT NULL,
  `PortID` bigint(20) NOT NULL,
  `SubmissionID` bigint(20) NOT NULL,
  PRIMARY KEY (`SubmissionIOID`),
  KEY `FK_lqkjpvbdc4hn18eu2o7eovx9j` (`DataID`),
  KEY `FK_4f63turcrhhoathgtlomuyh8c` (`PortID`),
  KEY `FK_4cp44e0btqspvnmjiwhdx7e92` (`SubmissionID`),
  CONSTRAINT `FK_4cp44e0btqspvnmjiwhdx7e92` FOREIGN KEY (`SubmissionID`) REFERENCES `Submission` (`SubmissionID`),
  CONSTRAINT `FK_4f63turcrhhoathgtlomuyh8c` FOREIGN KEY (`PortID`) REFERENCES `IOPort` (`PortID`),
  CONSTRAINT `FK_lqkjpvbdc4hn18eu2o7eovx9j` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`)
) ENGINE=InnoDB AUTO_INCREMENT=14725 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `UserID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Affiliation` varchar(255) DEFAULT NULL,
  `FirstName` varchar(255) DEFAULT NULL,
  `LastName` varchar(255) DEFAULT NULL,
  `LiferayID` varchar(255) DEFAULT NULL,
  `UserEmail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserApplication`
--

DROP TABLE IF EXISTS `UserApplication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserApplication` (
  `ApplicationID` bigint(20) NOT NULL,
  `UserID` bigint(20) NOT NULL,
  KEY `FK_p7bcuro0kqilirw2g7mn518ps` (`UserID`),
  KEY `FK_e8txnk0g9apsjen9tuqhdvwg7` (`ApplicationID`),
  CONSTRAINT `FK_e8txnk0g9apsjen9tuqhdvwg7` FOREIGN KEY (`ApplicationID`) REFERENCES `Application` (`ApplicationID`),
  CONSTRAINT `FK_p7bcuro0kqilirw2g7mn518ps` FOREIGN KEY (`UserID`) REFERENCES `User` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserAuthentication`
--

DROP TABLE IF EXISTS `UserAuthentication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserAuthentication` (
  `AuthenticationID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Authentication` longblob,
  `Session` varchar(255) DEFAULT NULL,
  `userLogin` varchar(255) DEFAULT NULL,
  `ResourceID` bigint(20) NOT NULL,
  `UserID` bigint(20) NOT NULL,
  PRIMARY KEY (`AuthenticationID`),
  KEY `FK_8ditcxsykcaxaluyyw8b1k7ny` (`ResourceID`),
  KEY `FK_hay2tbrhke61m1el2d2evtgmx` (`UserID`),
  CONSTRAINT `FK_8ditcxsykcaxaluyyw8b1k7ny` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`),
  CONSTRAINT `FK_hay2tbrhke61m1el2d2evtgmx` FOREIGN KEY (`UserID`) REFERENCES `User` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=187 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserPreference`
--

DROP TABLE IF EXISTS `UserPreference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserPreference` (
  `PrefID` bigint(20) NOT NULL,
  `UserID` bigint(20) NOT NULL,
  KEY `FK_f5jbqee62xbgj8dqddwrcgc33` (`UserID`),
  KEY `FK_stjhjspnve5r75xwwqc6enpwy` (`PrefID`),
  CONSTRAINT `FK_f5jbqee62xbgj8dqddwrcgc33` FOREIGN KEY (`UserID`) REFERENCES `User` (`UserID`),
  CONSTRAINT `FK_stjhjspnve5r75xwwqc6enpwy` FOREIGN KEY (`PrefID`) REFERENCES `Preference` (`PrefID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserProject`
--

DROP TABLE IF EXISTS `UserProject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserProject` (
  `ProjectID` bigint(20) NOT NULL,
  `UserID` bigint(20) NOT NULL,
  KEY `FK_erl0jrk0jypchdqv3155slm5g` (`UserID`),
  KEY `FK_7colhy93skdg1x8o7egyuirto` (`ProjectID`),
  CONSTRAINT `FK_7colhy93skdg1x8o7egyuirto` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FK_erl0jrk0jypchdqv3155slm5g` FOREIGN KEY (`UserID`) REFERENCES `User` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UsesIOPort`
--

DROP TABLE IF EXISTS `UsesIOPort`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UsesIOPort` (
  `DataID` bigint(20) NOT NULL,
  `IOPortID` bigint(20) NOT NULL,
  KEY `FK_58v8m4bjee47wh3oiphm08tbe` (`IOPortID`),
  KEY `FK_hojrsabi9u22oa9hxoxgwtuj6` (`DataID`),
  CONSTRAINT `FK_58v8m4bjee47wh3oiphm08tbe` FOREIGN KEY (`IOPortID`) REFERENCES `IOPort` (`PortID`),
  CONSTRAINT `FK_hojrsabi9u22oa9hxoxgwtuj6` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Value`
--

DROP TABLE IF EXISTS `Value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Value` (
  `ValueID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Value` varchar(255) DEFAULT NULL,
  `KeyID` bigint(20) NOT NULL,
  PRIMARY KEY (`ValueID`),
  KEY `FK_f7r27dwfxb8dh9e07m7ox8pnd` (`KeyID`),
  CONSTRAINT `FK_f7r27dwfxb8dh9e07m7ox8pnd` FOREIGN KEY (`KeyID`) REFERENCES `KeyName` (`KeyID`)
) ENGINE=InnoDB AUTO_INCREMENT=2953161 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueData`
--

DROP TABLE IF EXISTS `ValueData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueData` (
  `DataID` bigint(20) NOT NULL,
  `ValueID` bigint(20) NOT NULL,
  KEY `FK_ocyajpt4gfva1smhiyuh6ir65` (`ValueID`),
  KEY `FK_bwpvdj4v6htjslflu3w3xtdfv` (`DataID`),
  CONSTRAINT `FK_bwpvdj4v6htjslflu3w3xtdfv` FOREIGN KEY (`DataID`) REFERENCES `DataElement` (`DataID`),
  CONSTRAINT `FK_ocyajpt4gfva1smhiyuh6ir65` FOREIGN KEY (`ValueID`) REFERENCES `Value` (`ValueID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ValueProject`
--

DROP TABLE IF EXISTS `ValueProject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ValueProject` (
  `ProjectID` bigint(20) NOT NULL,
  `ValueID` bigint(20) NOT NULL,
  KEY `FK_nt3bimndeesy29j4rum4k1it0` (`ValueID`),
  KEY `FK_bgxlrsd6totr7b7mhm9dpf3kh` (`ProjectID`),
  CONSTRAINT `FK_bgxlrsd6totr7b7mhm9dpf3kh` FOREIGN KEY (`ProjectID`) REFERENCES `Project` (`ProjectID`),
  CONSTRAINT `FK_nt3bimndeesy29j4rum4k1it0` FOREIGN KEY (`ValueID`) REFERENCES `Value` (`ValueID`)
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

-- Dump completed on 2015-03-06 13:04:26
