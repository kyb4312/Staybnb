package com.staybnb.rooms.controller;

import com.staybnb.AbstractIntegrationTest;
import com.staybnb.common.auth.jwt.JwtUtils;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.dto.response.RoomResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.time.YearMonth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class RoomControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    JwtUtils jwtUtils;

    @BeforeAll
    void setup() {
        String token = jwtUtils.generateToken("2", "test");
        RestAssured.requestSpecification = given().header("Authorization", "Bearer " + token);
    }

    @Nested
    @DisplayName("getOneRoom Tests")
    class GetOneRoom {

        @Test
        @DisplayName("GetOne: 정상")
        public void testGetRoom() {
            long roomId = 1L;
            RoomResponse roomResponse =
                    given().log().all()
                            .port(port)
                            .when().get("/rooms/{roomId}", roomId)
                            .then().log().all()
                            .statusCode(200)
                            .extract().as(RoomResponse.class);

            assertThat(roomResponse.getId(), equalTo(roomId));
        }


        @Test
        @DisplayName("GetOne: 존재하지 않는 숙소 검색")
        public void testNonExistentRoomIdGetRoom() {
            long roomId = -1L;
            given().log().all()
                    .port(port)
                    .when().get("/rooms/{roomId}", roomId)
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("getAllRoom Tests")
    class GetAllRoom {

        @Test
        @DisplayName("GetAll: 정상")
        public void testGetRooms() {
            given().log().all()
                    .port(port)
                    .when().get("/rooms")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_OK)
                    .contentType(ContentType.JSON);
        }

        @Test
        @DisplayName("GetAll: PriceFrom > PriceTo 인 경우")
        public void testInvalidPriceRangeGetRooms() {
            given().log().all()
                    .port(port)
                    .when().get("/rooms?priceFrom=500000&priceTo=400000") // invalid @ValidPriceRange
                    .then().log().all()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .contentType(ContentType.JSON);
        }
    }

    @Nested
    @DisplayName("getPricing Tests")
    class GetPricing {

        @Test
        public void getPricing() {
            String path = String.format("/rooms/{roomId}/pricing?startDate=%s&endDate=%s&currency=KRW",
                    LocalDate.now().plusMonths(1).plusDays(1),
                    LocalDate.now().plusMonths(1).plusDays(2));

            PricingResponse pricingResponse = given().log().all()
                    .port(port)
                    .when().get(path, 1L)
                    .then().log().all()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().as(PricingResponse.class);

            assertThat(pricingResponse.getTotalPrice(), equalTo(300000.0));
        }
    }

    @Nested
    @DisplayName("getCalendar Tests")
    class GetCalendar {

        @Test
        public void getCalendar() {
            String path = String.format("/rooms/{roomId}/calendar?currency=KRW&yearMonth=%s", YearMonth.now());

            CalendarResponse response = given().log().all()
                    .port(port)
                    .when().get(path, 1L)
                    .then().log().all()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().as(CalendarResponse.class);

            assertThat(response.getDailyInfos().size(), equalTo(YearMonth.now().lengthOfMonth()));
        }
    }
}
