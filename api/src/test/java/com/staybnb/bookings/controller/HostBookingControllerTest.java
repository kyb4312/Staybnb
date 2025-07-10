package com.staybnb.bookings.controller;

import com.staybnb.AbstractIntegrationTest;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.common.jwt.JwtUtils;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

class HostBookingControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    JwtUtils jwtUtils;

    @BeforeAll
    void setup() {
        String token = jwtUtils.generateToken("2", "test");
        RestAssured.requestSpecification = given().header("Authorization", "Bearer " + token);
    }

    @Test
    void getBookings() {
        long roomId = 1L;

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