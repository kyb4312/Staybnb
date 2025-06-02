-- 전체 테이블 DDL
-- user
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` varchar(20) NOT NULL,
    `name` varchar(50) NOT NULL,
    `email` varchar(255) NOT NULL,
    `password` varchar(100) NOT NULL,
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_id_UNIQUE` (`user_id`)
);

-- place_type
CREATE TABLE `place_type` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(30) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`)
);

-- currency
CREATE TABLE `currency` (
    `code` varchar(3) NOT NULL,
    `name` varchar(20) NOT NULL,
    `symbol` varchar(5) NOT NULL,
    `exchange_rate` decimal(18,6) DEFAULT NULL,
    `last_updated` datetime DEFAULT NULL,
    PRIMARY KEY (`code`)
);

-- amenity
CREATE TABLE `amenity` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(30) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`)
);

-- room
CREATE TABLE `room` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `host_id` bigint NOT NULL,
    `place_type_id` int NOT NULL,
    `room_type` varchar(20) NOT NULL,
    `country` varchar(100) NOT NULL,
    `province` varchar(100) DEFAULT NULL,
    `city` varchar(200) NOT NULL,
    `street` varchar(200) NOT NULL,
    `flat` varchar(100) DEFAULT NULL,
    `max_number_of_guests` int NOT NULL,
    `bedrooms` int NOT NULL,
    `beds` int NOT NULL,
    `title` varchar(100) NOT NULL,
    `description` text,
    `price_per_night` int NOT NULL,
    `currency_code` varchar(3) NOT NULL,
    `is_deleted` tinyint DEFAULT NULL,
    `deleted_at` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_room_host_id_idx` (`host_id`),
    KEY `fk_room_place_type_id_idx` (`place_type_id`),
    KEY `fk_room_currency_idx` (`currency_code`),
    CONSTRAINT `fk_room_currency_code` FOREIGN KEY (`currency_code`) REFERENCES `currency` (`code`),
    CONSTRAINT `fk_room_host_id` FOREIGN KEY (`host_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_room_place_type_id` FOREIGN KEY (`place_type_id`) REFERENCES `place_type` (`id`)
)

-- room_amenity
CREATE TABLE `room_amenity` (
    `room_id` bigint NOT NULL,
    `amenity_id` int NOT NULL,
    PRIMARY KEY (`room_id`,`amenity_id`),
    KEY `fk_amenity_id_idx` (`amenity_id`),
    CONSTRAINT `fk_amenity_id` FOREIGN KEY (`amenity_id`) REFERENCES `amenity` (`id`),
    CONSTRAINT `fk_room_id` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
)

