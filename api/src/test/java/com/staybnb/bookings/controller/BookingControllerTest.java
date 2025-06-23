package com.staybnb.bookings.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.request.CreateBookingRequest;
import com.staybnb.bookings.dto.response.BookingPreviewResponse;
import com.staybnb.bookings.dto.response.BookingResponse;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerTest {

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
    void getBookingPreview() {
        long roomId = 1L;
        int numberOfGuests = 2;
        LocalDate checkIn = LocalDate.now().plusDays(15);
        LocalDate checkOut = LocalDate.now().plusDays(17);
        String guestCurrency = "KRW";

        BookingPreviewResponse response = given().log().all()
                .port(port)
                .when().get("/bookings/preview?roomId=" + roomId + "&numberOfGuests=" + numberOfGuests + "&checkIn=" + checkIn + "&checkOut=" + checkOut + "&guestCurrency=" + guestCurrency)
                .then().log().all()
                .statusCode(200)
                .extract().as(BookingPreviewResponse.class);

        BookingPreviewResponse expected = new BookingPreviewResponse(
                roomId, checkIn, checkOut, numberOfGuests, 600_000.0, guestCurrency);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void createBooking() {
        long roomId = 1L;
        long guestId = 1L;
        LocalDate checkIn = LocalDate.now().plusDays(13);
        LocalDate checkOut = LocalDate.now().plusDays(15);
        int numberOfGuests = 2;
        double bookingPrice = 600_000.0;
        String guestCurrency = "KRW";

        CreateBookingRequest request = new CreateBookingRequest(roomId, guestId, checkIn, checkOut, numberOfGuests, bookingPrice, guestCurrency);

        BookingResponse response = given().log().all()
                .port(port)
                .body(request)
                .contentType(ContentType.JSON)
                .when().post("/bookings")
                .then().log().all()
                .statusCode(201)
                .extract().as(BookingResponse.class);

        BookingResponse expected = new BookingResponse(null, roomId, guestId, checkIn, checkOut, numberOfGuests, bookingPrice, guestCurrency, BookingStatus.REQUESTED.toString());

        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);

        // 같은 날짜에 예약 재시도시 에러
        given().log().all()
                .port(port)
                .body(request)
                .contentType(ContentType.JSON)
                .when().post("/bookings")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void getBooking() {
        long bookingId = 1L;

        BookingResponse response = given().log().all()
                .port(port)
                .when().get("/bookings/" + bookingId)
                .then().log().all()
                .statusCode(200)
                .extract().as(BookingResponse.class);

        assertThat(response)
                .usingRecursiveComparison()
                .isNotNull();
    }

    @Test
    void cancelBooking() {
        long bookingId = 1L;

        BookingResponse response = given().log().all()
                .port(port)
                .when().delete("/bookings/" + bookingId)
                .then().log().all()
                .statusCode(200)
                .extract().as(BookingResponse.class);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED.toString());
        assertThat(response)
                .usingRecursiveComparison()
                .isNotNull();
    }

    @Test
    void findUpcomingBookings() throws JsonProcessingException {
        long userId = 2L;

        given().log().all()
                .port(port)
                .when().get("/bookings/upcoming/" + userId)
                .then().log().all()
                .statusCode(200)
                .body("content.status",
                        everyItem(anyOf(
                                equalTo(BookingStatus.REQUESTED.toString()),
                                equalTo(BookingStatus.RESERVED.toString())
                        )));
    }

    @Test
    void findPastBookings() throws JsonProcessingException {
        long userId = 2L;

        given().log().all()
                .port(port)
                .when().get("/bookings/past/" + userId)
                .then().log().all()
                .statusCode(200)
                .body("content.status",
                        everyItem(equalTo(BookingStatus.ENDED.toString())));
    }

    @Test
    void findCancelledBookings() {
        long userId = 2L;

        given().log().all()
                .port(port)
                .when().get("/bookings/cancelled/" + userId)
                .then().log().all()
                .statusCode(200)
                .body("content.status",
                        everyItem(anyOf(
                                equalTo(BookingStatus.CANCELLED.toString()),
                                equalTo(BookingStatus.REJECTED.toString())
                        )));
    }
}