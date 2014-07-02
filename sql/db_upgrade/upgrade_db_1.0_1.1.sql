-- MySQL dump 10.13  Distrib 5.1.71, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: neuroscience
-- ------------------------------------------------------
-- Server version	5.1.69

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
  `BaseURI` varchar(255) DEFAULT NULL,
  `Robot` bit(1) DEFAULT NULL,
  `Protocol` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Resource`
--

LOCK TABLES `Resource` WRITE;
/*!40000 ALTER TABLE `Resource` DISABLE KEYS */;
INSERT INTO `Resource` VALUES (1,'xnatZ0','xnatZ0','','\0',NULL,'\0','http'),(2,'Vlemed','Vl-med VO on Dutch Grid','\0','\0',NULL,'','lcg'),(3,'AmcCluster','AMC cluster','\0','\0',NULL,'','scp'),(4,'LocalServer','Local Cluster: Orange Server','\0','\0',NULL,'','cp'),(5,'glite-vlemed','glite-vlemed','\0','\0',NULL,'','lcg'),(6,'local;dci-bridge host(64bit)','local;dci-bridge host(64bit)','\0','\0',NULL,'','cp'),(7,'glite;vlemed','glite;vlemed','','','lfn:/grid/vlemed/NeuroscienceGateway/neurodev','','lcg');
/*!40000 ALTER TABLE `Resource` ENABLE KEYS */;
UNLOCK TABLES;

-- Dump completed on 2014-04-14 18:07:30
-- MySQL dump 10.13  Distrib 5.1.71, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: neuroscience
-- ------------------------------------------------------
-- Server version	5.1.69

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
-- Adding extra field to `DataElement` table
---

ALTER TABLE `DataElement` ADD `Size` INT(60);


--
-- Table structure for table `UserAuthentication`
--

DROP TABLE IF EXISTS `UserAuthentication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserAuthentication` (
  `AuthenticationID` bigint(20) NOT NULL AUTO_INCREMENT,
  `UserID` varchar(255) DEFAULT NULL,
  `UserLogin` longblob,
  `Session` varchar(255) DEFAULT NULL,
  `UserKey` bigint(20) NOT NULL,
  `ResourceID` bigint(20) NOT NULL,
  PRIMARY KEY (`AuthenticationID`),
  UNIQUE KEY `UserID` (`UserID`),
  KEY `FK60EF5743F614057C` (`ResourceID`),
  KEY `FK60EF574376A5ECC4` (`UserKey`),
  CONSTRAINT `FK60EF574376A5ECC4` FOREIGN KEY (`UserKey`) REFERENCES `User` (`UserKey`),
  CONSTRAINT `FK60EF5743F614057C` FOREIGN KEY (`ResourceID`) REFERENCES `Resource` (`ResourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=latin1;



/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserAuthentication`
--
INSERT INTO `UserAuthentication`( `UserID`, `Authentication`, `Session`, `UserKey`, `ResourceID`) SELECT `UserID`, `Authentication`, `Session`,`UserKey`, `ResourceID` FROM User U JOIN Resource R
WHERE R.ResourceID=1

--
-- Creating triggers
--
CREATE TRIGGER `ComputeStatus` AFTER INSERT ON `Submission`
 FOR EACH ROW Begin
         Declare count numeric default 0;
         Declare indexM int default 0;
         Declare idMaster int default 0;
         Declare Stat varchar(50);
         Declare FinalStat varchar(250) default '';
                 DECLARE cursor1 CURSOR FOR SELECT Distinct Status from Submission where ProcessingID=New.ProcessingID order by Status;

		OPEN cursor1;
		set idMaster = (Select FOUND_ROWS());
		while indexM<idMaster do
			FETCH cursor1 INTO Stat;
			set count = (select count(Status) from Submission where ProcessingID=New.ProcessingID and Status=Stat);
			if count>0 then
				SET FinalStat = concat(FinalStat, count, ' ', Stat, '; ');
			END IF;

			set indexM=indexM+1;
        end while;
        CLOSE cursor1;

        IF (length(FinalStat)>2) then
                set FinalStat=substring(FinalStat, 1, length(FinalStat)-2);
        END IF;


        Update Processing set ProcessingStatus=FinalStat where ProcessingID=New.ProcessingID;
        Update Processing set ProcessingLastUpdate = now() where  ProcessingID=New.ProcessingID;
	END

CREATE TRIGGER `computeStatus2` AFTER UPDATE ON `Submission`
 FOR EACH ROW Begin
         Declare count numeric default 0;
         Declare indexM int default 0;
         Declare idMaster int default 0;
         Declare Stat varchar(50);
         Declare FinalStat varchar(250) default '';
                 DECLARE cursor1 CURSOR FOR SELECT Distinct Status from Submission where ProcessingID=New.ProcessingID order by Status;

		OPEN cursor1;
		set idMaster = (Select FOUND_ROWS());
		while indexM<idMaster do
			FETCH cursor1 INTO Stat;
			set count = (select count(Status) from Submission where ProcessingID=New.ProcessingID and Status=Stat);
			if count>0 then
				SET FinalStat = concat(FinalStat, count, ' ', Stat, '; ');
			END IF;

			set indexM=indexM+1;
        end while;
        CLOSE cursor1;

        IF (length(FinalStat)>2) then
                set FinalStat=substring(FinalStat, 1, length(FinalStat)-2);
        END IF;


        Update Processing set ProcessingStatus=FinalStat where ProcessingID=New.ProcessingID;
        Update Processing set ProcessingLastUpdate = now() where  ProcessingID=New.ProcessingID;
     END
