-- 전체 테이블 DDL
-- user
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL,
    `email` varchar(255) NOT NULL,
    `password`   varchar(255) NOT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
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
    `name`       varchar(50) DEFAULT NULL,
    `exchange_rate` double NOT NULL,
    `updated_at` datetime    DEFAULT NULL,
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
    `currency_code` varchar(3) NOT NULL,
    `base_price` int    NOT NULL,
    `base_price_in_usd` double NOT NULL,
    `is_deleted` bit(1) NOT NULL,
    `deleted_at` datetime DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_room_place_type_id_idx` (`place_type_id`),
    KEY `fk_room_currency_idx` (`currency_code`),
    KEY          `fk_host_id_idx` (`host_id`),
    CONSTRAINT `fk_room_currency_code` FOREIGN KEY (`currency_code`) REFERENCES `currency` (`code`),
    CONSTRAINT `fk_room_host_id` FOREIGN KEY (`host_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_room_place_type_id` FOREIGN KEY (`place_type_id`) REFERENCES `place_type` (`id`)
);

-- room_amenity
CREATE TABLE `room_amenity` (
    `room_id` bigint NOT NULL,
    `amenity_id` int NOT NULL,
    PRIMARY KEY (`room_id`,`amenity_id`),
    KEY `fk_amenity_id_idx` (`amenity_id`),
    CONSTRAINT `fk_room_amenity_amenity_id` FOREIGN KEY (`amenity_id`) REFERENCES `amenity` (`id`),
    CONSTRAINT `fk_room_amenity_room_id` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
);

-- pricing
CREATE TABLE `pricing`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `room_id`         bigint NOT NULL,
    `start_date`      date   NOT NULL,
    `end_date`        date   NOT NULL,
    `price_per_night` int    NOT NULL,
    `updated_at`      datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY               `fk_room_id_idx` (`room_id`),
    CONSTRAINT `fk_pricing_room_id` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
);

-- availability
CREATE TABLE `availability`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `room_id`      bigint NOT NULL,
    `start_date`   date   NOT NULL,
    `end_date`     date   NOT NULL,
    `is_available` bit(1) NOT NULL,
    `updated_at`   datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY            `fk_availability_room_id_idx` (`room_id`),
    CONSTRAINT `fk_availability_room_id` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
);

-- reservation
CREATE TABLE `reservation`
(
    `id`         int         NOT NULL,
    `room_id`    bigint      NOT NULL,
    `guest_id`   bigint      NOT NULL,
    `price` double NOT NULL,
    `currency`   varchar(3)  NOT NULL,
    `status`     varchar(20) NOT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY          `fk_reservation_room_id_idx` (`room_id`),
    KEY          `fk_reservation_guest_id_idx` (`guest_id`),
    CONSTRAINT `fk_reservation_guest_id` FOREIGN KEY (`guest_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_reservation_room_id` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
);
