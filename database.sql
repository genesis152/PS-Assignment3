-- MySQL dump 10.13  Distrib 8.0.13, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: mydb
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.11-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `parcel`
--

DROP TABLE IF EXISTS `parcel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `parcel` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(45) NOT NULL,
  `coordinates` varchar(45) NOT NULL,
  `date` datetime NOT NULL,
  `assignedPostmanID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `assignedPostmanID_idx` (`assignedPostmanID`),
  CONSTRAINT `assignedPostmanID` FOREIGN KEY (`assignedPostmanID`) REFERENCES `user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parcel`
--

LOCK TABLES `parcel` WRITE;
/*!40000 ALTER TABLE `parcel` DISABLE KEYS */;
INSERT INTO `parcel` VALUES (2,'Cluj','(10,10)','2020-04-15 02:23:41',4),(3,'Oradea','(2,4)','2020-04-16 15:06:43',4),(8,'Deva1','(0,5)','2020-04-16 15:31:07',4),(9,'Timisoara','(4,4)','2020-04-16 15:31:07',4),(10,'Constanta','(6,4)','2020-04-16 17:18:34',4),(32,'Madrid','(12,24)','2020-05-11 18:57:30',17),(38,'Italia','(10,12)','2020-05-14 17:28:12',4);
/*!40000 ALTER TABLE `parcel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `password` varchar(32) NOT NULL,
  `username` varchar(45) NOT NULL,
  `type` enum('POSTMAN','COORDINATOR','ADMINISTRATOR') NOT NULL,
  PRIMARY KEY (`ID`,`username`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (4,'JohnDoe','1a1dc91c907325c69271ddf0c944bc72','John','POSTMAN'),(7,'Cicada','332de775a36bbfcb140e1caa06299a8a','Cicada','COORDINATOR'),(13,'admin','63a9f0ea7bb98050796b649e85481845','admin','ADMINISTRATOR'),(17,'Maria','4297f44b13955235245b2497399d7a93','Maria1','POSTMAN'),(29,'Cicada2','202cb962ac59075b964b07152d234b70','Cicada123','COORDINATOR'),(31,'123123','4297f44b13955235245b2497399d7a93','123123','POSTMAN');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-05-14 19:45:08
