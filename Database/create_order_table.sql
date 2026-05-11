CREATE DATABASE IF NOT EXISTS gonature_db;
USE gonature_db;

CREATE TABLE IF NOT EXISTS `Order` (
    order_number INT PRIMARY KEY,
    order_date DATE,
    number_of_visitors INT,
    confirmation_code INT,
    subscriber_id INT,
    date_of_placing_order DATE
);

INSERT INTO `Order`
(order_number, order_date, number_of_visitors, confirmation_code, subscriber_id, date_of_placing_order)
VALUES
(1, '11-05-2026', 6, 123, 100, '01-05-2026')
ON DUPLICATE KEY UPDATE
    order_date = VALUES(order_date),
    number_of_visitors = VALUES(number_of_visitors),
    confirmation_code = VALUES(confirmation_code),
    subscriber_id = VALUES(subscriber_id),
    date_of_placing_order = VALUES(date_of_placing_order);
