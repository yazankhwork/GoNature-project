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
