package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.vo.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HostBookingControllerTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("staybnb")
            .withUsername("test")
            .withPassword("test");
    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // jpa 에서 ddl 실행 방지
        registry.add("spring.sql.init.mode", () -> "always"); // schema.sql, data.sql 스크립트 실행
    }

    @Test
    void getBookings() {
        long roomId = 2L;

        given().log().all()
                .port(port)
                .when().get("/host/bookings/listings/" + roomId)
                .then().log().all()
                .statusCode(200)
                .body("content.roomId", everyItem(equalTo((int) roomId)));
    }

    @Test
    void updateBooking() {
        long bookingId = 1L; // status == REQUESTED
        String status = BookingStatus.REJECTED.toString();

        given().log().all()
                .port(port)
                .body(status)
                .when().patch("/host/bookings/" + bookingId)
                .then().log().all()
                .statusCode(200)
                .body("status", equalTo(status));
    }

    @Test
    void updateBookingFailed() {
        long bookingId = 5L; // status == ENDED
        String status = BookingStatus.REJECTED.toString();

        given().log().all()
                .port(port)
                .body(status)
                .when().patch("/host/bookings/" + bookingId)
                .then().log().all()
                .statusCode(400);
    }
}