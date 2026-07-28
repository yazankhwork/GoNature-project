-- ============================================================================
-- GoNature database schema — verified against the uploaded Java project
-- MySQL 8.0+
--
-- Reviewed against:
--   server.dao.BookingDAO
--   server.dao.DiscountDAO
--   server.dao.EmployeeDAO
--   server.dao.NotificationDAO
--   server.dao.ParkDAO
--   server.dao.ReportDAO
--   server.dao.VisitorDAO
--   server.dao.WaitingListDAO
--   server.database.DatabaseController
--   server.network.GoNatureServer
--   common.Booking / Visitor / Parks
--
-- This file contains structure only and is safe to publish on GitHub.
-- Run schema.sql first, then sample_data.sql.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `gonature_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `gonature_db`;

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `waitinglist`;
DROP TABLE IF EXISTS `discount_requests`;
DROP TABLE IF EXISTS `park_change_requests`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `subscriptions`;
DROP TABLE IF EXISTS `employees`;
DROP TABLE IF EXISTS `visitors`;
DROP TABLE IF EXISTS `parks`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Parent tables
-- ============================================================================

CREATE TABLE `parks` (
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_capacity` int NOT NULL DEFAULT 150,
  `booking_percent` int NOT NULL DEFAULT 80,
  `visit_duration_hours` int NOT NULL DEFAULT 4,

  PRIMARY KEY (`park_name`),

  CONSTRAINT `chk_parks_capacity`
    CHECK (`max_capacity` > 0),
  CONSTRAINT `chk_parks_booking_percent`
    CHECK (`booking_percent` BETWEEN 0 AND 100),
  CONSTRAINT `chk_parks_visit_duration`
    CHECK (`visit_duration_hours` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `visitors` (
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_guide` tinyint(1) NOT NULL DEFAULT 0,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,

  PRIMARY KEY (`visitor_id`),
  KEY `idx_visitors_username` (`username`),

  CONSTRAINT `chk_visitors_is_guide`
    CHECK (`is_guide` IN (0, 1))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `employees` (
  `emp_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `job` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,

  PRIMARY KEY (`emp_id`),
  KEY `idx_employees_park_name` (`park_name`),
  KEY `idx_employees_role` (`role`),

  CONSTRAINT `fk_employees_park`
    FOREIGN KEY (`park_name`)
    REFERENCES `parks` (`park_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `chk_employees_role`
    CHECK (`role` IN (
      'SERVICE_REP',
      'ENTRY_WORKER',
      'PARK_MANAGER',
      'DEPT_MANAGER'
    ))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- Visitor and booking tables
-- ============================================================================

CREATE TABLE `subscriptions` (
  `sub_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `family_members` int NOT NULL,
  `payment_method` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credit_card` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`sub_id`),
  UNIQUE KEY `uk_subscription_visitor` (`visitor_id`),

  CONSTRAINT `fk_subscriptions_visitor`
    FOREIGN KEY (`visitor_id`)
    REFERENCES `visitors` (`visitor_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `chk_subscriptions_family_members`
    CHECK (`family_members` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `visit_date` date NOT NULL,
  `visit_time` time NOT NULL,
  `visitors_count` int NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Pending',
  `total_price` int NOT NULL DEFAULT 0,
  `booking_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Regular Visitor',
  `confirmation_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checkin_time` datetime DEFAULT NULL,
  `checkout_time` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `reminder_sent_at` datetime DEFAULT NULL,
  `confirmation_deadline` datetime DEFAULT NULL,
  `is_guide_group` tinyint(1) NOT NULL DEFAULT 0,
  `is_subscriber` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `telephone` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',

  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `uk_bookings_confirmation_code` (`confirmation_code`),
  KEY `idx_bookings_visitor_date` (`visitor_id`, `visit_date`, `visit_time`),
  KEY `idx_bookings_capacity`
    (`park_name`, `visit_date`, `status`, `visit_time`),
  KEY `idx_bookings_reports`
    (`park_name`, `visit_date`, `status`),

  CONSTRAINT `fk_bookings_visitor`
    FOREIGN KEY (`visitor_id`)
    REFERENCES `visitors` (`visitor_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_bookings_park`
    FOREIGN KEY (`park_name`)
    REFERENCES `parks` (`park_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `chk_bookings_visitors_count`
    CHECK (`visitors_count` > 0),
  CONSTRAINT `chk_bookings_total_price`
    CHECK (`total_price` >= 0),
  CONSTRAINT `chk_bookings_status`
    CHECK (`status` IN (
      'Pending',
      'Confirmed',
      'Entered',
      'Exited',
      'Cancelled'
    )),
  CONSTRAINT `chk_bookings_type`
    CHECK (`booking_type` IN ('Guide', 'Regular Visitor')),
  CONSTRAINT `chk_bookings_is_guide_group`
    CHECK (`is_guide_group` IN (0, 1)),
  CONSTRAINT `chk_bookings_is_subscriber`
    CHECK (`is_subscriber` IN (0, 1)),
  CONSTRAINT `chk_bookings_checkout_after_checkin`
    CHECK (
      `checkout_time` IS NULL
      OR `checkin_time` IS NULL
      OR `checkout_time` >= `checkin_time`
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `waitinglist` (
  `waiting_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `visit_date` date NOT NULL,
  `visit_time` time NOT NULL,
  `visitors_count` int NOT NULL,
  `visitor_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notified_time` timestamp NULL DEFAULT NULL,
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `telephone` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',

  PRIMARY KEY (`waiting_id`),
  UNIQUE KEY `uk_waitinglist_visitor_slot`
    (`visitor_id`, `park_name`, `visit_date`, `visit_time`),
  KEY `idx_waitinglist_queue`
    (`notified_time`, `visit_date`, `visit_time`, `request_time`, `waiting_id`),
  KEY `idx_waitinglist_capacity`
    (`park_name`, `visit_date`, `notified_time`, `visit_time`),
  KEY `idx_waitinglist_visitor_offer`
    (`visitor_id`, `notified_time`),

  CONSTRAINT `fk_waitinglist_visitor`
    FOREIGN KEY (`visitor_id`)
    REFERENCES `visitors` (`visitor_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_waitinglist_park`
    FOREIGN KEY (`park_name`)
    REFERENCES `parks` (`park_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `chk_waitinglist_visitors_count`
    CHECK (`visitors_count` > 0),
  CONSTRAINT `chk_waitinglist_type`
    CHECK (`visitor_type` IN ('Guide', 'Regular Visitor'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `visitor_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `booking_id` int DEFAULT NULL,
  `notification_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Sent',

  PRIMARY KEY (`notification_id`),
  KEY `idx_notifications_visitor_recent`
    (`visitor_id`, `sent_at`, `notification_id`),
  KEY `idx_notifications_booking_id` (`booking_id`),

  CONSTRAINT `fk_notifications_visitor`
    FOREIGN KEY (`visitor_id`)
    REFERENCES `visitors` (`visitor_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_notifications_booking`
    FOREIGN KEY (`booking_id`)
    REFERENCES `bookings` (`booking_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- Manager request tables
-- ============================================================================

CREATE TABLE `park_change_requests` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_by` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_capacity` int NOT NULL,
  `requested_booking_percent` int NOT NULL,
  `requested_visit_duration_hours` int NOT NULL,
  `request_type` varchar(50) COLLATE utf8mb4_unicode_ci
    NOT NULL DEFAULT 'PARK_PARAMS_CHANGE',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci
    NOT NULL DEFAULT 'Pending',
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `decision_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `decision_time` timestamp NULL DEFAULT NULL,

  PRIMARY KEY (`request_id`),
  KEY `idx_park_change_pending`
    (`status`, `request_time`, `request_id`),
  KEY `idx_park_change_park` (`park_name`),
  KEY `idx_park_change_requested_by` (`requested_by`),
  KEY `idx_park_change_decision_by` (`decision_by`),

  CONSTRAINT `fk_park_change_requests_park`
    FOREIGN KEY (`park_name`)
    REFERENCES `parks` (`park_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_park_change_requests_requested_by`
    FOREIGN KEY (`requested_by`)
    REFERENCES `employees` (`emp_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_park_change_requests_decision_by`
    FOREIGN KEY (`decision_by`)
    REFERENCES `employees` (`emp_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `chk_park_change_capacity`
    CHECK (`requested_capacity` > 0),
  CONSTRAINT `chk_park_change_booking_percent`
    CHECK (`requested_booking_percent` BETWEEN 0 AND 100),
  CONSTRAINT `chk_park_change_duration`
    CHECK (`requested_visit_duration_hours` > 0),
  CONSTRAINT `chk_park_change_type`
    CHECK (`request_type` = 'PARK_PARAMS_CHANGE'),
  CONSTRAINT `chk_park_change_status`
    CHECK (`status` IN ('Pending', 'Approved', 'Rejected')),
  CONSTRAINT `chk_park_change_decision`
    CHECK (
      (`status` = 'Pending' AND `decision_by` IS NULL AND `decision_time` IS NULL)
      OR
      (`status` IN ('Approved', 'Rejected')
       AND `decision_by` IS NOT NULL
       AND `decision_time` IS NOT NULL)
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `discount_requests` (
  `discount_request_id` int NOT NULL AUTO_INCREMENT,
  `park_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_by` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_percent` int NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci
    NOT NULL DEFAULT 'Pending',
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `decision_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `decision_time` timestamp NULL DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,

  PRIMARY KEY (`discount_request_id`),
  KEY `idx_discount_pending`
    (`status`, `request_time`, `discount_request_id`),
  KEY `idx_discount_active`
    (`park_name`, `status`, `start_date`, `end_date`),
  KEY `idx_discount_requested_by` (`requested_by`),
  KEY `idx_discount_decision_by` (`decision_by`),

  CONSTRAINT `fk_discount_requests_park`
    FOREIGN KEY (`park_name`)
    REFERENCES `parks` (`park_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_discount_requests_requested_by`
    FOREIGN KEY (`requested_by`)
    REFERENCES `employees` (`emp_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `fk_discount_requests_decision_by`
    FOREIGN KEY (`decision_by`)
    REFERENCES `employees` (`emp_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT `chk_discount_percent`
    CHECK (`discount_percent` BETWEEN 1 AND 100),
  CONSTRAINT `chk_discount_date_range`
    CHECK (`start_date` <= `end_date`),
  CONSTRAINT `chk_discount_status`
    CHECK (`status` IN ('Pending', 'Approved', 'Rejected')),
  CONSTRAINT `chk_discount_decision`
    CHECK (
      (`status` = 'Pending' AND `decision_by` IS NULL AND `decision_time` IS NULL)
      OR
      (`status` IN ('Approved', 'Rejected')
       AND `decision_by` IS NOT NULL
       AND `decision_time` IS NOT NULL)
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
