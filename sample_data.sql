-- ============================================================================
-- GoNature fictional sample data
-- Run schema.sql before this file.
--
-- All IDs, names, emails, phone numbers, passwords, and payment details are
-- fictional and intended only for development, demonstrations, and portfolios.
-- ============================================================================

USE `gonature_db`;

START TRANSACTION;

-- ============================================================================
-- Parks
-- ============================================================================

INSERT INTO `parks`
(`park_name`, `max_capacity`, `booking_percent`, `visit_duration_hours`)
VALUES
('Banias Park', 100, 80, 4),
('Carmel Park', 150, 80, 4),
('Hula Valley', 130, 80, 4),
('Jordan Park', 120, 80, 4),
('Ramon Crater', 150, 80, 4),
('Safari Zoo', 200, 80, 4);


-- ============================================================================
-- Visitors
-- ============================================================================

INSERT INTO `visitors`
(`visitor_id`, `username`, `password`, `email`, `phone`,
 `is_guide`, `full_name`)
VALUES
('CASUAL', NULL, '', NULL, NULL, 0, 'Casual Visitor'),
('900000001', 'demoVisitor', 'demo1234',
 'visitor1@example.com', '0500000001', 0, 'Demo Visitor'),
('900000002', 'demoGuide', 'demo1234',
 'guide1@example.com', '0500000002', 1, 'Demo Guide'),
('900000003', 'demoSubscriber', 'demo1234',
 'subscriber@example.com', '0500000003', 0, 'Demo Subscriber'),
('900000004', 'waitingVisitor', 'demo1234',
 'waiting@example.com', '0500000004', 0, 'Waiting Visitor'),
('900000005', 'reportVisitor', 'demo1234',
 'report@example.com', '0500000005', 0, 'Report Visitor');


-- ============================================================================
-- Employees
-- ============================================================================

INSERT INTO `employees`
(`emp_id`, `password`, `first_name`, `last_name`, `full_name`,
 `email`, `phone`, `park_name`, `role`, `job`)
VALUES
('800000001', 'demo1234', 'Service', 'Representative',
 'Service Representative', 'service@example.com', '0501000001', NULL,
 'SERVICE_REP', 'Service Representative'),

('800000002', 'demo1234', 'Carmel', 'Entrance Worker',
 'Carmel Entrance Worker', 'entry.carmel@example.com', '0501000002',
 'Carmel Park', 'ENTRY_WORKER', 'Entrance Worker'),

('800000003', 'demo1234', 'Carmel', 'Park Manager',
 'Carmel Park Manager', 'manager.carmel@example.com', '0501000003',
 'Carmel Park', 'PARK_MANAGER', 'Park Manager'),

('800000004', 'demo1234', 'Department', 'Manager',
 'Department Manager', 'department.manager@example.com', '0501000004',
 NULL, 'DEPT_MANAGER', 'Department Manager'),

('800000005', 'demo1234', 'Banias', 'Park Manager',
 'Banias Park Manager', 'manager.banias@example.com', '0501000005',
 'Banias Park', 'PARK_MANAGER', 'Park Manager'),

('800000006', 'demo1234', 'Banias', 'Entrance Worker',
 'Banias Entrance Worker', 'entry.banias@example.com', '0501000006',
 'Banias Park', 'ENTRY_WORKER', 'Entrance Worker');


-- ============================================================================
-- Subscription
-- ============================================================================

INSERT INTO `subscriptions`
(`sub_id`, `visitor_id`, `first_name`, `last_name`, `full_name`,
 `phone`, `email`, `family_members`, `payment_method`, `credit_card`,
 `created_at`)
VALUES
(1, '900000003', 'Demo', 'Subscriber', 'Demo Subscriber',
 '0500000003', 'subscriber@example.com', 4, 'Cash', NULL,
 '2026-07-01 09:00:00');


-- ============================================================================
-- Bookings
-- ============================================================================

INSERT INTO `bookings`
(`booking_id`, `visitor_id`, `park_name`, `visit_date`, `visit_time`,
 `visitors_count`, `email`, `status`, `total_price`, `booking_type`,
 `confirmation_code`, `checkin_time`, `checkout_time`, `cancelled_at`,
 `reminder_sent_at`, `confirmation_deadline`, `is_guide_group`,
 `is_subscriber`, `created_at`, `telephone`)
VALUES
-- Future regular booking
(1, '900000001', 'Carmel Park', '2026-08-10', '10:00:00',
 2, 'visitor1@example.com', 'Confirmed', 51, 'Regular Visitor',
 '100001', NULL, NULL, NULL, '2026-08-09 09:00:00',
 '2026-08-09 11:00:00', 0, 0, '2026-07-20 10:00:00',
 '0500000001'),

-- Future guided group booking
(2, '900000002', 'Banias Park', '2026-08-12', '11:00:00',
 10, 'guide1@example.com', 'Pending', 270, 'Guide',
 '100002', NULL, NULL, NULL, NULL, NULL,
 1, 0, '2026-07-21 12:00:00', '0500000002'),

-- Future subscriber booking
(3, '900000003', 'Hula Valley', '2026-08-15', '09:00:00',
 4, 'subscriber@example.com', 'Confirmed', 92, 'Regular Visitor',
 '100003', NULL, NULL, NULL, '2026-08-14 08:00:00',
 '2026-08-14 10:00:00', 0, 1, '2026-07-22 13:00:00',
 '0500000003'),

-- Historical casual visit used by entry/exit reports
(4, 'CASUAL', 'Carmel Park', '2026-07-15', '13:30:00',
 1, NULL, 'Exited', 30, 'Regular Visitor',
 '100004', '2026-07-15 13:30:00', '2026-07-15 16:45:00',
 NULL, NULL, NULL, 0, 0, '2026-07-15 13:30:00', ''),

-- Historical guided visit used by visit-type reports
(5, '900000002', 'Banias Park', '2026-07-17', '09:00:00',
 8, 'guide1@example.com', 'Exited', 216, 'Guide',
 '100005', '2026-07-17 09:05:00', '2026-07-17 12:30:00',
 NULL, NULL, NULL, 1, 0, '2026-07-10 10:00:00',
 '0500000002'),

-- Historical cancelled booking
(6, '900000001', 'Carmel Park', '2026-07-20', '12:00:00',
 3, 'visitor1@example.com', 'Cancelled', 77, 'Regular Visitor',
 '100006', NULL, NULL, '2026-07-19 15:00:00',
 NULL, NULL, 0, 0, '2026-07-10 11:00:00', '0500000001'),

-- Historical unfulfilled booking used by the no-show report
(7, '900000005', 'Carmel Park', '2026-07-21', '14:00:00',
 1, 'report@example.com', 'Pending', 26, 'Regular Visitor',
 '100007', NULL, NULL, NULL, NULL, NULL,
 0, 0, '2026-07-11 11:00:00', '0500000005');


-- ============================================================================
-- Waiting list
-- ============================================================================

INSERT INTO `waitinglist`
(`waiting_id`, `visitor_id`, `park_name`, `visit_date`, `visit_time`,
 `visitors_count`, `visitor_type`, `email`, `notified_time`,
 `request_time`, `telephone`)
VALUES
(1, '900000004', 'Carmel Park', '2026-08-10', '10:00:00',
 3, 'Regular Visitor', 'waiting@example.com', NULL,
 '2026-07-23 14:00:00', '0500000004');


-- ============================================================================
-- Notifications
-- ============================================================================

INSERT INTO `notifications`
(`notification_id`, `visitor_id`, `booking_id`, `notification_type`,
 `message_text`, `email`, `phone`, `sent_at`, `status`)
VALUES
(1, '900000001', 1, 'BOOKING_CONFIRMATION',
 'Your demo booking was confirmed.',
 'visitor1@example.com', '0500000001',
 '2026-07-20 10:01:00', 'Sent'),

(2, '900000003', 3, 'BOOKING_CONFIRMATION',
 'Your subscriber demo booking was confirmed.',
 'subscriber@example.com', '0500000003',
 '2026-07-22 13:01:00', 'Sent'),

(3, '900000004', NULL, 'WAITING_LIST_JOINED',
 'You were added to the demo waiting list.',
 'waiting@example.com', '0500000004',
 '2026-07-23 14:01:00', 'Sent');


-- ============================================================================
-- Park-change requests
-- ============================================================================

INSERT INTO `park_change_requests`
(`request_id`, `park_name`, `requested_by`, `requested_capacity`,
 `requested_booking_percent`, `requested_visit_duration_hours`,
 `request_type`, `status`, `request_time`, `decision_by`,
 `decision_time`)
VALUES
(1, 'Carmel Park', '800000003', 170, 85, 4,
 'PARK_PARAMS_CHANGE', 'Pending',
 '2026-07-24 09:00:00', NULL, NULL),

(2, 'Banias Park', '800000005', 110, 80, 5,
 'PARK_PARAMS_CHANGE', 'Approved',
 '2026-07-18 09:00:00', '800000004', '2026-07-18 12:00:00');


-- ============================================================================
-- Discount requests
-- ============================================================================

INSERT INTO `discount_requests`
(`discount_request_id`, `park_name`, `requested_by`, `discount_name`,
 `discount_percent`, `status`, `request_time`, `decision_by`,
 `decision_time`, `start_date`, `end_date`)
VALUES
(1, 'Banias Park', '800000005', 'Demo Summer Discount',
 10, 'Approved', '2026-07-19 09:00:00', '800000004',
 '2026-07-19 11:00:00', '2026-08-01', '2026-08-31'),

(2, 'Carmel Park', '800000003', 'Demo Student Discount',
 5, 'Pending', '2026-07-25 10:00:00', NULL,
 NULL, '2026-09-01', '2026-09-30');

COMMIT;
