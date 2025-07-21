-- 전체 테이블 DDL

-- gist 인덱스로 기본 자료형도 사용 필요해서 추가함
CREATE
EXTENSION IF NOT EXISTS btree_gist;

-- user
CREATE TABLE "user"
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    email      VARCHAR(255)       NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_deleted bool DEFAULT false NOT NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- place_type
CREATE TABLE place_type
(
    id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

-- exchange_rate
CREATE TABLE exchange_rate
(
    currency   VARCHAR(3) PRIMARY KEY,
    rate       DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP DEFAULT NULL
);

-- amenity
CREATE TABLE amenity
(
    id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

-- room
CREATE TABLE room
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id              BIGINT           NOT NULL,
    place_type_id        INTEGER          NOT NULL,
    room_type            VARCHAR(20)      NOT NULL,
    country              VARCHAR(100)     NOT NULL,
    province             VARCHAR(100) DEFAULT NULL,
    city                 VARCHAR(200)     NOT NULL,
    street               VARCHAR(200)     NOT NULL,
    flat                 VARCHAR(100) DEFAULT NULL,
    max_number_of_guests INTEGER          NOT NULL,
    bedrooms             INTEGER          NOT NULL,
    beds                 INTEGER          NOT NULL,
    title                VARCHAR(100)     NOT NULL,
    description          TEXT,
    currency             VARCHAR(3)       NOT NULL,
    base_price           INTEGER          NOT NULL,
    base_price_in_usd    DOUBLE PRECISION NOT NULL,
    is_deleted           BOOLEAN          NOT NULL,
    deleted_at           TIMESTAMP    DEFAULT NULL,
    created_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    time_zone_id VARCHAR(50) NOT NULL,

    CONSTRAINT fk_room_host_id FOREIGN KEY (host_id) REFERENCES "user" (id),
    CONSTRAINT fk_room_place_type_id FOREIGN KEY (place_type_id) REFERENCES place_type (id)
);

-- room_amenity
CREATE TABLE room_amenity
(
    room_id    BIGINT  NOT NULL,
    amenity_id INTEGER NOT NULL,
    PRIMARY KEY (room_id, amenity_id),

    CONSTRAINT fk_room_amenity_room_id FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_room_amenity_amenity_id FOREIGN KEY (amenity_id) REFERENCES amenity (id)
);

-- pricing
CREATE TABLE pricing
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id         BIGINT    NOT NULL,
    date_range      DATERANGE NOT NULL,
    price_per_night INTEGER   NOT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pricing_room_id FOREIGN KEY (room_id) REFERENCES room (id),

    CONSTRAINT      no_overlapping_pricing_date_range EXCLUDE USING GIST (room_id WITH =, date_range WITH &&)
);

-- availability
CREATE TABLE availability
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id      BIGINT    NOT NULL,
    date_range   DATERANGE NOT NULL,
    is_available BOOLEAN   NOT NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_availability_room_id FOREIGN KEY (room_id) REFERENCES room (id),

    CONSTRAINT   no_overlapping_availbility_date_range EXCLUDE USING GIST (room_id WITH =, date_range WITH &&)
);

-- booking
CREATE TABLE booking
(
    id               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id          BIGINT           NOT NULL,
    guest_id         BIGINT           NOT NULL,
    date_range       DATERANGE        NOT NULL,
    number_of_guests INTEGER          NOT NULL,
    booking_price    DOUBLE PRECISION NOT NULL,
    currency         VARCHAR(3)       NOT NULL,
    status           VARCHAR(20)      NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT NULL,
    time_zone_id VARCHAR(50) NOT NULL,

    CONSTRAINT fk_booking_room_id FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_booking_guest_id FOREIGN KEY (guest_id) REFERENCES "user" (id),

    CONSTRAINT no_overlapping_booking_date_range EXCLUDE USING gist (room_id WITH =, date_range WITH &&) WHERE (status IN ('REQUESTED', 'RESERVED', 'ENDED'))
);

CREATE TABLE timezone_midnight
(
    time_zone_id TEXT PRIMARY KEY,
    utc_midnight TIME      NOT NULL,
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- 타임존 관련 인덱스
CREATE INDEX idx_timezone_midnight_utc_time ON timezone_midnight (utc_midnight);
CREATE INDEX idx_room_timezone ON room (time_zone_id);
CREATE INDEX idx_booking_timezone_status ON booking (time_zone_id, status);

-- FK 일부에 대한 인덱스
CREATE INDEX idx_room_place_type_id ON room (place_type_id);
CREATE INDEX idx_room_host_id ON room (host_id);
CREATE INDEX idx_booking_guest_id ON booking (guest_id);