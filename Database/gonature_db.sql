-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: gonature_db
-- ------------------------------------------------------
-- Server version	8.0.45


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `visit_date` date DEFAULT NULL,
  `visit_time` time DEFAULT NULL,
  `visitors_count` int DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_price` int DEFAULT '0',
  `booking_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confirmation_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `checkin_time` datetime DEFAULT NULL,
  `checkout_time` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `reminder_sent_at` datetime DEFAULT NULL,
  `confirmation_deadline` datetime DEFAULT NULL,
  `is_guide_group` int DEFAULT '0',
  `is_subscriber` int DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `telephone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT '',
  PRIMARY KEY (`booking_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,'123456789','Carmel Park','2026-06-19','10:00:00',2,'test@gonature.com','Pending',51,'Regular Visitor','100001',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-19 16:20:20',''),(2,'212263404','Carmel Park','2026-06-20','10:00:00',1,'georgeabosini@gonature.com','Confirmed',26,'Regular Visitor','167248',NULL,NULL,NULL,'2026-06-19 19:23:36','2026-06-19 21:23:36',0,0,'2026-06-19 16:23:32',''),(3,'CASUAL','Carmel Park','2026-06-19','19:40:00',1,NULL,'Entered',30,'Regular Visitor','916848',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-19 16:40:45',''),(4,'212263404','Carmel Park','2026-06-20','10:00:00',14,'georgeabosini@gonature.com','Confirmed',357,'Regular Visitor','191197',NULL,NULL,NULL,'2026-06-19 19:46:21','2026-06-19 21:46:21',0,0,'2026-06-19 16:46:18',''),(5,'212263404','Carmel Park','2026-06-20','10:00:00',14,'georgeabosini@gonature.com','Confirmed',357,'Regular Visitor','619299',NULL,NULL,NULL,'2026-06-19 19:46:25','2026-06-19 21:46:25',0,0,'2026-06-19 16:46:23',''),(6,'212263404','Carmel Park','2026-06-20','10:00:00',14,'georgeabosini@gonature.com','Confirmed',357,'Regular Visitor','756533',NULL,NULL,NULL,'2026-06-19 19:46:28','2026-06-19 21:46:28',0,0,'2026-06-19 16:46:26',''),(7,'212263404','Carmel Park','2026-06-20','10:00:00',15,'georgeabosini@gonature.com','Confirmed',383,'Regular Visitor','672187',NULL,NULL,NULL,'2026-06-19 19:46:41','2026-06-19 21:46:41',0,0,'2026-06-19 16:46:39',''),(8,'212263404','Carmel Park','2026-06-20','10:00:00',15,'georgeabosini@gonature.com','Confirmed',383,'Regular Visitor','949616',NULL,NULL,NULL,'2026-06-19 19:46:45','2026-06-19 21:46:45',0,0,'2026-06-19 16:46:43',''),(9,'212263404','Carmel Park','2026-06-20','10:00:00',15,'georgeabosini@gonature.com','Cancelled',383,'Regular Visitor','635808',NULL,NULL,'2026-06-20 13:26:55','2026-06-19 19:46:48','2026-06-19 21:46:48',0,0,'2026-06-19 16:46:46',''),(10,'212263404','Carmel Park','2026-06-20','10:00:00',15,'georgeabosini@gonature.com','Confirmed',383,'Regular Visitor','188203',NULL,NULL,NULL,'2026-06-19 19:46:51','2026-06-19 21:46:51',0,0,'2026-06-19 16:46:50',''),(11,'212263404','Carmel Park','2026-06-20','10:00:00',15,'georgeabosini@gonature.com','Cancelled',383,'Regular Visitor','875684',NULL,NULL,'2026-06-19 19:47:19','2026-06-19 19:46:55','2026-06-19 21:46:55',0,0,'2026-06-19 16:46:53',''),(12,'212263404','Carmel Park','2026-06-20','10:00:00',2,'georgeabosini@gonature.com','Confirmed',51,'Regular Visitor','275620',NULL,NULL,NULL,'2026-06-19 19:47:02','2026-06-19 21:47:02',0,0,'2026-06-19 16:46:59',''),(13,'212263404','Carmel Park','2026-06-20','10:00:00',13,'georgeabosini@gonature.com','Cancelled',332,'Regular Visitor','199350',NULL,NULL,'2026-06-19 20:00:01','2026-06-19 19:47:25','2026-06-19 21:47:25',0,0,'2026-06-19 16:47:25',''),(14,'123456789','Carmel Park','2026-06-20','10:00:00',2,'test@gonature.com','Confirmed',51,'Regular Visitor','576465',NULL,NULL,NULL,'2026-06-19 19:49:19','2026-06-19 21:49:19',0,0,'2026-06-19 16:49:17',''),(15,'212263404','Carmel Park','2026-06-20','10:00:00',1,'george@gmail.com','Confirmed',13,'Regular Visitor','424789',NULL,NULL,NULL,'2026-06-19 21:04:36','2026-06-19 23:04:36',0,0,'2026-06-19 18:04:34',''),(16,'212263404','Carmel Park','2026-06-24','10:00:00',1,'george@aaa.com','Confirmed',13,'Regular Visitor','131325',NULL,NULL,NULL,'2026-06-23 10:11:14','2026-06-23 12:11:14',0,0,'2026-06-19 18:09:01',''),(17,'212263404','Carmel Park','2026-06-24','10:00:00',1,'george@aa.com','Confirmed',26,'Regular Visitor','797453',NULL,NULL,NULL,'2026-06-23 10:11:14','2026-06-23 12:11:14',0,0,'2026-06-19 18:36:56',''),(18,'212263404','Carmel Park','2026-06-20','10:00:00',12,'george@aaa.com','Cancelled',306,'Regular Visitor','703229',NULL,NULL,'2026-06-19 21:42:08','2026-06-19 21:39:41','2026-06-19 23:39:41',0,0,'2026-06-19 18:39:39',''),(19,'222222222','Carmel Park','2026-06-20','10:00:00',2,'aaa@aaaaaaa.com','Cancelled',51,'Regular Visitor','795719',NULL,NULL,'2026-06-20 12:30:21','2026-06-19 21:42:46','2026-06-19 23:42:46',0,0,'2026-06-19 18:42:42',''),(20,'CASUAL','Banias Park','2026-06-19','21:57:00',1,NULL,'Entered',30,'Regular Visitor','717509',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-19 18:57:52',''),(21,'CASUAL','Banias Park','2026-06-19','21:57:00',1,NULL,'Entered',30,'Regular Visitor','864137',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-19 18:57:57',''),(22,'212263404','Carmel Park','2026-06-25','10:00:00',1,'aaaa@aaa.com','Pending',30,'Regular Visitor','325614',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-20 09:35:13',''),(23,'212263404','Carmel Park','2026-06-21','10:00:00',1,'aaa@bbb.com','Confirmed',26,'Regular Visitor','828382',NULL,NULL,NULL,'2026-06-20 12:54:14','2026-06-20 14:54:14',0,0,'2026-06-20 09:54:13',''),(24,'212263404','Carmel Park','2026-06-22','10:00:00',1,'george@aaa.com','Pending',26,'Regular Visitor','140691',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-20 09:58:38',''),(25,'212263404','Carmel Park','2026-06-22','10:00:00',1,'george@aaa.com','Pending',26,'Regular Visitor','154334',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-20 09:58:48','1234123412'),(26,'123123444','Carmel Park','2026-06-21','10:00:00',1,'george@abc.com','Confirmed',23,'Regular Visitor','508419',NULL,NULL,NULL,'2026-06-20 13:03:02','2026-06-20 15:03:02',0,1,'2026-06-20 10:03:00',''),(27,'212263404','Carmel Park','2026-06-22','10:00:00',1,'george@qqq.com','Pending',26,'Regular Visitor','158949',NULL,NULL,NULL,NULL,NULL,0,0,'2026-06-20 10:03:22',''),(28,'212263404','Carmel Park','2026-06-21','10:00:00',12,'george@abc.com','Cancelled',306,'Regular Visitor','641988',NULL,NULL,'2026-06-20 13:30:49','2026-06-20 13:09:11','2026-06-20 15:09:11',0,0,'2026-06-20 10:09:09','0524891808'),(29,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Confirmed',383,'Regular Visitor','878734',NULL,NULL,NULL,'2026-06-20 13:09:16','2026-06-20 15:09:16',0,0,'2026-06-20 10:09:15','0524891808'),(30,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Confirmed',383,'Regular Visitor','270031',NULL,NULL,NULL,'2026-06-20 13:09:19','2026-06-20 15:09:19',0,0,'2026-06-20 10:09:18','0524891808'),(31,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Confirmed',383,'Regular Visitor','721415',NULL,NULL,NULL,'2026-06-20 13:09:24','2026-06-20 15:09:24',0,0,'2026-06-20 10:09:23','0524891808'),(32,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Confirmed',383,'Regular Visitor','499538',NULL,NULL,NULL,'2026-06-20 13:09:28','2026-06-20 15:09:28',0,0,'2026-06-20 10:09:26','0524891808'),(33,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Cancelled',383,'Regular Visitor','679079',NULL,NULL,'2026-06-20 13:11:41','2026-06-20 13:09:33','2026-06-20 15:09:33',0,0,'2026-06-20 10:09:29','0524891808'),(34,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Confirmed',383,'Regular Visitor','642446',NULL,NULL,NULL,'2026-06-20 13:09:37','2026-06-20 15:09:37',0,0,'2026-06-20 10:09:36','0524891808'),(35,'212263404','Carmel Park','2026-06-21','10:00:00',15,'george@abc.com','Confirmed',383,'Regular Visitor','779803',NULL,NULL,NULL,'2026-06-20 13:09:40','2026-06-20 15:09:40',0,0,'2026-06-20 10:09:39','0524891808'),(36,'212263404','Carmel Park','2026-06-21','10:00:00',1,'george@abc.com','Confirmed',26,'Regular Visitor','343675',NULL,NULL,NULL,'2026-06-20 13:09:45','2026-06-20 15:09:45',0,0,'2026-06-20 10:09:43','0524891808'),(37,'123123444','Carmel Park','2026-06-21','10:00:00',4,'bbb@bbb.com','Confirmed',92,'Regular Visitor','838672',NULL,NULL,NULL,'2026-06-20 13:11:56','2026-06-20 15:11:56',0,1,'2026-06-20 10:11:53',''),(38,'123123444','Carmel Park','2026-06-21','10:00:00',7,'aaa@aaa.com','Confirmed',161,'Regular Visitor','212291',NULL,NULL,NULL,'2026-06-20 13:12:23','2026-06-20 15:12:23',0,1,'2026-06-20 10:12:21',''),(39,'123123111','Carmel Park','2026-06-21','10:00:00',4,'aaa@abc.com','Confirmed',92,'Regular Visitor','940568',NULL,NULL,NULL,'2026-06-20 13:29:21','2026-06-20 15:29:21',0,1,'2026-06-20 10:29:19',''),(40,'123123111','Carmel Park','2026-06-21','10:00:00',1,'aaa@abc.com','Cancelled',23,'Regular Visitor','121022',NULL,NULL,'2026-06-22 15:45:53','2026-06-20 13:31:22','2026-06-20 15:31:22',0,1,'2026-06-20 10:31:20',''),(41,'212263404','Carmel Park','2026-06-24','10:00:00',1,'aaa@qwe.com','Cancelled',26,'Regular Visitor','997042',NULL,NULL,'2026-06-23 10:25:21','2026-06-23 10:11:14','2026-06-23 12:11:14',0,0,'2026-06-20 10:56:25','');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discount_requests`
--

DROP TABLE IF EXISTS `discount_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discount_requests` (
  `discount_request_id` int NOT NULL AUTO_INCREMENT,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `requested_by` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `discount_percent` int NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Pending',
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `decision_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `decision_time` timestamp NULL DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  PRIMARY KEY (`discount_request_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discount_requests`
--

LOCK TABLES `discount_requests` WRITE;
/*!40000 ALTER TABLE `discount_requests` DISABLE KEYS */;
INSERT INTO `discount_requests` VALUES (3,'Banian Park','555555555','summer',10,'Approved','2026-06-20 10:07:34','444444444','2026-06-20 10:07:43','2026-06-20','2026-06-27');
/*!40000 ALTER TABLE `discount_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `emp_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `job` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`emp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES ('111111111','1234','Service','Representative','Service Representative','service@gonature.com',NULL,'SERVICE_REP','Service Representative'),('222222222','1234','Entrance','Worker','Entrance Worker','entry@gonature.com','Carmel Park','ENTRY_WORKER','Entrance Worker'),('333333333','1234','Park','Manager','Park Manager','parkmanager@gonature.com','Carmel Park','PARK_MANAGER','Park Manager'),('444444444','1234','Department','Manager','Department Manager','deptmanager@gonature.com',NULL,'DEPT_MANAGER','Department Manager'),('555555555','1234','Park','Manager','Park Manager','parkmanager@gonature.com','Banian Park','PARK_MANAGER','Park Manager');
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `booking_id` int DEFAULT NULL,
  `notification_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message_text` text COLLATE utf8mb4_unicode_ci,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'Sent',
  PRIMARY KEY (`notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'212263404',2,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 1\nConfirmation Code: 167248\nTotal Price: 26 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:23:32','Sent'),(2,'212263404',2,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:23:36','Sent'),(3,'212263404',4,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 14\nConfirmation Code: 191197\nTotal Price: 357 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:18','Sent'),(4,'212263404',4,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 14\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:21','Sent'),(5,'212263404',5,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 14\nConfirmation Code: 619299\nTotal Price: 357 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:23','Sent'),(6,'212263404',5,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 14\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:25','Sent'),(7,'212263404',6,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 14\nConfirmation Code: 756533\nTotal Price: 357 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:27','Sent'),(8,'212263404',6,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 14\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:28','Sent'),(9,'212263404',7,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 15\nConfirmation Code: 672187\nTotal Price: 383 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:39','Sent'),(10,'212263404',7,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:41','Sent'),(11,'212263404',8,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 15\nConfirmation Code: 949616\nTotal Price: 383 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:43','Sent'),(12,'212263404',8,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:45','Sent'),(13,'212263404',9,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 15\nConfirmation Code: 635808\nTotal Price: 383 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:46','Sent'),(14,'212263404',9,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:48','Sent'),(15,'212263404',10,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 15\nConfirmation Code: 188203\nTotal Price: 383 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:50','Sent'),(16,'212263404',10,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:51','Sent'),(17,'212263404',11,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 15\nConfirmation Code: 875684\nTotal Price: 383 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:53','Sent'),(18,'212263404',11,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:55','Sent'),(19,'212263404',12,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 2\nConfirmation Code: 275620\nTotal Price: 51 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:46:59','Sent'),(20,'212263404',12,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 2\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:47:02','Sent'),(21,'212263404',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 13\nYou have 1 hour to make the booking before it passes to the next visitor.','georgeabosini@gonature.com',NULL,'2026-06-19 16:47:19','Sent'),(22,'212263404',13,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 13\nConfirmation Code: 199350\nTotal Price: 332 ILS','georgeabosini@gonature.com',NULL,'2026-06-19 16:47:25','Sent'),(23,'212263404',13,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 13\nPlease confirm or cancel within 2 hours.','georgeabosini@gonature.com',NULL,'2026-06-19 16:47:25','Sent'),(24,'123456789',14,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 2\nConfirmation Code: 576465\nTotal Price: 51 ILS','test@gonature.com','0500000000','2026-06-19 16:49:17','Sent'),(25,'123456789',14,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 2\nPlease confirm or cancel within 2 hours.','test@gonature.com','0500000000','2026-06-19 16:49:19','Sent'),(26,'123456789',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 3\nYou have 1 hour to make the booking before it passes to the next visitor.','test@gonature.com','0500000000','2026-06-19 17:00:01','Sent'),(27,'222222222',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 13:00\nVisitors: 1\nYou have 1 hour to make the booking before it passes to the next visitor.','guide@gonature.com','0500000001','2026-06-19 17:00:01','Sent'),(28,'123456789',NULL,'WAITING_LIST_EXPIRED','Your waiting-list offer expired because you did not claim it within 1 hour.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 3\nThe spot was passed to the next visitor in the waiting list.','test@gonature.com','0500000000','2026-06-19 18:00:25','Sent'),(29,'212263404',15,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 1\nConfirmation Code: 424789\nTotal Price: 13 ILS','george@gmail.com',NULL,'2026-06-19 18:04:34','Sent'),(30,'212263404',15,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','george@gmail.com',NULL,'2026-06-19 18:04:36','Sent'),(31,'212263404',16,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-24\nTime: 10:00\nVisitors: 1\nConfirmation Code: 131325\nTotal Price: 13 ILS','george@aaa.com',NULL,'2026-06-19 18:09:01','Sent'),(32,'212263404',17,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-24\nTime: 10:00\nVisitors: 1\nConfirmation Code: 797453\nTotal Price: 26 ILS','george@aa.com',NULL,'2026-06-19 18:36:56','Sent'),(33,'212263404',18,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 12\nConfirmation Code: 703229\nTotal Price: 306 ILS','george@aaa.com',NULL,'2026-06-19 18:39:39','Sent'),(34,'212263404',18,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 12\nPlease confirm or cancel within 2 hours.','george@aaa.com',NULL,'2026-06-19 18:39:41','Sent'),(35,'123456789',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 3\nYou have 1 hour to make the booking before it passes to the next visitor.','aaa@aaa.com','0500000000','2026-06-19 18:42:08','Sent'),(36,'222222222',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 2\nYou have 1 hour to make the booking before it passes to the next visitor.','aaa@aaaaaaa.com','0500000001','2026-06-19 18:42:10','Sent'),(37,'222222222',19,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00\nVisitors: 2\nConfirmation Code: 795719\nTotal Price: 51 ILS','aaa@aaaaaaa.com','0500000001','2026-06-19 18:42:42','Sent'),(38,'222222222',19,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 2\nPlease confirm or cancel within 2 hours.','aaa@aaaaaaa.com','0500000001','2026-06-19 18:42:46','Sent'),(39,'123456789',NULL,'WAITING_LIST_VISIT_PASSED','Your waiting-list request was closed because the visit time passed.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 3','aaa@aaa.com','0500000000','2026-06-20 09:30:21','Sent'),(40,'222222222',19,'AUTO_CANCEL','Your booking was automatically cancelled because you did not confirm in time.\nPark: Carmel Park\nDate: 2026-06-20\nTime: 10:00:00\nVisitors: 2','aaa@aaaaaaa.com',NULL,'2026-06-20 09:30:21','Sent'),(41,'212263404',22,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-25\nTime: 10:00\nVisitors: 1\nConfirmation Code: 325614\nTotal Price: 30 ILS','aaaa@aaa.com',NULL,'2026-06-20 09:35:13','Sent'),(42,'212263404',23,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 1\nConfirmation Code: 828382\nTotal Price: 26 ILS','aaa@bbb.com',NULL,'2026-06-20 09:54:13','Sent'),(43,'212263404',23,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','aaa@bbb.com',NULL,'2026-06-20 09:54:14','Sent'),(44,'212263404',24,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-22\nTime: 10:00\nVisitors: 1\nConfirmation Code: 140691\nTotal Price: 26 ILS','george@aaa.com',NULL,'2026-06-20 09:58:38','Sent'),(45,'212263404',25,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-22\nTime: 10:00\nVisitors: 1\nConfirmation Code: 154334\nTotal Price: 26 ILS','george@aaa.com','1234123412','2026-06-20 09:58:48','Sent'),(46,'123123444',26,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 1\nConfirmation Code: 508419\nTotal Price: 23 ILS','george@abc.com','','2026-06-20 10:03:00','Sent'),(47,'123123444',26,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','george@abc.com','','2026-06-20 10:03:02','Sent'),(48,'212263404',27,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-22\nTime: 10:00\nVisitors: 1\nConfirmation Code: 158949\nTotal Price: 26 ILS','george@qqq.com',NULL,'2026-06-20 10:03:22','Sent'),(49,'212263404',28,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 12\nConfirmation Code: 641988\nTotal Price: 306 ILS','george@abc.com','0524891808','2026-06-20 10:09:09','Sent'),(50,'212263404',28,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 12\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:11','Sent'),(51,'212263404',29,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 878734\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:15','Sent'),(52,'212263404',29,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:16','Sent'),(53,'212263404',30,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 270031\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:18','Sent'),(54,'212263404',30,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:19','Sent'),(55,'212263404',31,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 721415\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:23','Sent'),(56,'212263404',31,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:24','Sent'),(57,'212263404',32,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 499538\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:26','Sent'),(58,'212263404',32,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:28','Sent'),(59,'212263404',33,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 679079\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:29','Sent'),(60,'212263404',33,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:33','Sent'),(61,'212263404',34,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 642446\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:36','Sent'),(62,'212263404',34,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:37','Sent'),(63,'212263404',35,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 15\nConfirmation Code: 779803\nTotal Price: 383 ILS','george@abc.com','0524891808','2026-06-20 10:09:39','Sent'),(64,'212263404',35,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 15\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:40','Sent'),(65,'212263404',36,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 1\nConfirmation Code: 343675\nTotal Price: 26 ILS','george@abc.com','0524891808','2026-06-20 10:09:43','Sent'),(66,'212263404',36,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','george@abc.com','0524891808','2026-06-20 10:09:45','Sent'),(67,'123123111',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 4\nYou have 2 hours to make the booking before it passes to the next visitor.','aaa@aaa.com',NULL,'2026-06-20 10:11:41','Sent'),(68,'123123444',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 4\nYou have 2 hours to make the booking before it passes to the next visitor.','bbb@bbb.com','','2026-06-20 10:11:41','Sent'),(69,'123123444',37,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 4\nConfirmation Code: 838672\nTotal Price: 92 ILS','bbb@bbb.com','','2026-06-20 10:11:54','Sent'),(70,'123123444',37,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 4\nPlease confirm or cancel within 2 hours.','bbb@bbb.com','','2026-06-20 10:11:56','Sent'),(71,'123123444',38,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 7\nConfirmation Code: 212291\nTotal Price: 161 ILS','aaa@aaa.com','','2026-06-20 10:12:21','Sent'),(72,'123123444',38,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 7\nPlease confirm or cancel within 2 hours.','aaa@aaa.com','','2026-06-20 10:12:23','Sent'),(73,'123123111',39,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 4\nConfirmation Code: 940568\nTotal Price: 92 ILS','aaa@abc.com',NULL,'2026-06-20 10:29:19','Sent'),(74,'123123111',39,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 4\nPlease confirm or cancel within 2 hours.','aaa@abc.com',NULL,'2026-06-20 10:29:21','Sent'),(75,'123123444',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 8\nYou have 2 hours to make the booking before it passes to the next visitor.','aaa@aaa.com','','2026-06-20 10:30:49','Sent'),(76,'123123111',NULL,'WAITING_LIST_AVAILABLE','A place opened for your waiting-list request.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 1\nYou have 2 hours to make the booking before it passes to the next visitor.','aaa@abc.com',NULL,'2026-06-20 10:31:10','Sent'),(77,'123123111',40,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00\nVisitors: 1\nConfirmation Code: 121022\nTotal Price: 23 ILS','aaa@abc.com',NULL,'2026-06-20 10:31:20','Sent'),(78,'123123111',40,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','aaa@abc.com',NULL,'2026-06-20 10:31:22','Sent'),(79,'212263404',41,'BOOKING_CONFIRMATION','Booking approved.\nPark: Carmel Park\nDate: 2026-06-24\nTime: 10:00\nVisitors: 1\nConfirmation Code: 997042\nTotal Price: 26 ILS','aaa@qwe.com',NULL,'2026-06-20 10:56:26','Sent'),(80,'123123111',40,'AUTO_CANCEL','Your booking was automatically cancelled because you did not confirm in time.\nPark: Carmel Park\nDate: 2026-06-21\nTime: 10:00:00\nVisitors: 1','aaa@abc.com',NULL,'2026-06-22 12:45:53','Sent'),(81,'212263404',16,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-24\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','george@aaa.com',NULL,'2026-06-23 07:11:14','Sent'),(82,'212263404',17,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-24\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','george@aa.com',NULL,'2026-06-23 07:11:14','Sent'),(83,'212263404',41,'VISIT_REMINDER','Reminder: your visit is tomorrow.\nPark: Carmel Park\nDate: 2026-06-24\nTime: 10:00:00\nVisitors: 1\nPlease confirm or cancel within 2 hours.','aaa@qwe.com',NULL,'2026-06-23 07:11:14','Sent');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `park_change_requests`
--

DROP TABLE IF EXISTS `park_change_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `park_change_requests` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_by` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_capacity` int DEFAULT NULL,
  `requested_booking_percent` int DEFAULT NULL,
  `requested_visit_duration_hours` int DEFAULT NULL,
  `request_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CAPACITY_CHANGE',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Pending',
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `decision_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `decision_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`request_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park_change_requests`
--

LOCK TABLES `park_change_requests` WRITE;
/*!40000 ALTER TABLE `park_change_requests` DISABLE KEYS */;
INSERT INTO `park_change_requests` VALUES (1,'Banias Park','800000003',100,80,4,'PARK_PARAMS_CHANGE','Approved','2026-06-19 17:04:13','800000004','2026-06-19 17:05:03'),(2,'Ramon Crater','333333333',150,80,4,'PARK_PARAMS_CHANGE','Rejected','2026-06-19 18:52:00','444444444','2026-06-19 18:52:18');
/*!40000 ALTER TABLE `park_change_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parks`
--

DROP TABLE IF EXISTS `parks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parks` (
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_capacity` int NOT NULL DEFAULT '150',
  `booking_percent` int NOT NULL DEFAULT '80',
  `visit_duration_hours` int NOT NULL DEFAULT '4',
  PRIMARY KEY (`park_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parks`
--

LOCK TABLES `parks` WRITE;
/*!40000 ALTER TABLE `parks` DISABLE KEYS */;
INSERT INTO `parks` VALUES ('Banias Park',100,80,4),('Carmel Park',150,80,4),('Hula Valley',130,80,4),('Jordan Park',120,80,4),('Ramon Crater',150,80,4),('Safari Zoo',200,80,4);
/*!40000 ALTER TABLE `parks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriptions`
--

DROP TABLE IF EXISTS `subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriptions` (
  `sub_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `family_members` int DEFAULT NULL,
  `payment_method` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credit_card` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sub_id`),
  UNIQUE KEY `uk_subscription_visitor` (`visitor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriptions`
--

LOCK TABLES `subscriptions` WRITE;
/*!40000 ALTER TABLE `subscriptions` DISABLE KEYS */;
INSERT INTO `subscriptions` VALUES (1,'123451234','rabea','r','rabea r','05211111111111','rabea@gonature.com',5,'Cash','','2026-06-19 17:13:00'),(2,'123123111','ana','ana','ana ana','0777861287','aaa@aaa.com',2,'Credit Card','1234123412341234','2026-06-19 18:46:45'),(3,'123123444','george','nset','george nset','1111111111','george@abc.com',4,'Cash','','2026-06-20 09:59:48');
/*!40000 ALTER TABLE `subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitors`
--

DROP TABLE IF EXISTS `visitors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitors` (
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_guide` int DEFAULT '0',
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`visitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitors`
--

LOCK TABLES `visitors` WRITE;
/*!40000 ALTER TABLE `visitors` DISABLE KEYS */;
INSERT INTO `visitors` VALUES ('123123111','aaa','1234','aaa@gonature.com',NULL,1,'aaa'),('123123123','guideuser','','guide@gonature.com','0500000001',1,'Guide Group'),('123123444','abc','1234','george@abc.com','',0,'abc'),('123412341',NULL,'12345','yazan@gonature.com',NULL,1,'yazan'),('123456780','yazanK','1234','y@k.com','',0,'yazanK'),('123456789','testuser','','test@gonature.com','0500000000',0,'Test Visitor'),('212263404','george','1234','george@gonature.com',NULL,0,'george');
/*!40000 ALTER TABLE `visitors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waitinglist`
--

DROP TABLE IF EXISTS `waitinglist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waitinglist` (
  `waiting_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `visit_date` date DEFAULT NULL,
  `visit_time` time DEFAULT NULL,
  `visitors_count` int DEFAULT NULL,
  `visitor_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notified_time` timestamp NULL DEFAULT NULL,
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `telephone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT '',
  PRIMARY KEY (`waiting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waitinglist`
--

LOCK TABLES `waitinglist` WRITE;
/*!40000 ALTER TABLE `waitinglist` DISABLE KEYS */;
/*!40000 ALTER TABLE `waitinglist` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-23 11:52:08
