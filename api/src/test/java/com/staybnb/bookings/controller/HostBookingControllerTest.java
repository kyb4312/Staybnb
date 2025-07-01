package com.staybnb.bookings.controller;

import com.staybnb.bookings.domain.vo.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HostBookingControllerTest {

    @LocalServerPort
    int port;

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