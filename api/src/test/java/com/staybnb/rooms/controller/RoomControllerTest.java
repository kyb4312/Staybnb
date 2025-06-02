package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.vo.Address;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomControllerTest {

    @LocalServerPort
    int port;

    @Test
    public void testGetRoom() {
        testCreateRoom();

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
    public void testGetRooms() {
        given().log().all()
                .port(port)
                .when().get("/rooms?startDate=2025-05-25&endDate=2025-05-26&currency=KRW") // TODO: null 예외 처리 필요..
                .then().log().all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON);
    }

    @Test
    public void testCreateRoom() {
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Set<String> amenities = new HashSet<>();
        amenities.add("wifi");
        amenities.add("kitchen");
        amenities.add("air conditioner");
        amenities.add("tv");

        CreateRoomRequest createRoomRequest = CreateRoomRequest.builder()
                .hostId(1L)
                .placeType("house")
                .roomType("ENTIRE_PLACE")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency("KRW")
                .build();

        RoomResponse response =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/rooms")
                        .then().log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class);

        assertThat(response.getId(), notNullValue());

        RoomResponse expected = RoomResponse.builder()
                .hostId(createRoomRequest.getHostId())
                .placeType(createRoomRequest.getPlaceType())
                .roomType(createRoomRequest.getRoomType())
                .address(createRoomRequest.getAddress())
                .maxNumberOfGuests(createRoomRequest.getMaxNumberOfGuests()) // updated
                .bedrooms(createRoomRequest.getBedrooms())
                .beds(createRoomRequest.getBeds())
                .amenities(createRoomRequest.getAmenities())
                .title(createRoomRequest.getTitle())
                .description(createRoomRequest.getDescription())
                .pricePerNight(createRoomRequest.getPricePerNight()) // updated
                .currency(createRoomRequest.getCurrency()) // updated
                .build();

        log.info("createRoomRequest: {}", createRoomRequest);
        log.info("toDomain: {}", createRoomRequest.toCommand());
        log.info("response: {}", response);

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);

    }

    @Test
    public void testUpdateRoom() {
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Set<String> amenities = new HashSet<>();
        amenities.add("wifi");
        amenities.add("kitchen");
        amenities.add("air conditioner");
        amenities.add("tv");

        CreateRoomRequest createRoomRequest = CreateRoomRequest.builder()
                .hostId(1L)
                .placeType("house")
                .roomType("ENTIRE_PLACE")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency("KRW")
                .build();

        long roomId =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/rooms")
                        .then().log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class)
                        .getId();

        UpdateRoomRequest updateRoomRequest = UpdateRoomRequest.builder()
                .maxNumberOfGuests(4)
                .pricePerNight(900_000)
                .currency("KRW")
                .build();

        RoomResponse response =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(updateRoomRequest)
                        .when().patch("/rooms/{roomId}", roomId)
                        .then().log().all()
                        .statusCode(HttpStatus.SC_OK)
                        .extract().as(RoomResponse.class);

        RoomResponse expected = RoomResponse.builder()
                .id(roomId)
                .hostId(createRoomRequest.getHostId())
                .placeType(createRoomRequest.getPlaceType())
                .roomType(createRoomRequest.getRoomType())
                .address(createRoomRequest.getAddress())
                .maxNumberOfGuests(updateRoomRequest.getMaxNumberOfGuests()) // updated
                .bedrooms(createRoomRequest.getBedrooms())
                .beds(createRoomRequest.getBeds())
                .amenities(createRoomRequest.getAmenities())
                .title(createRoomRequest.getTitle())
                .description(createRoomRequest.getDescription())
                .pricePerNight(updateRoomRequest.getPricePerNight()) // updated
                .currency(updateRoomRequest.getCurrency()) // updated
                .build();

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    public void testDeleteRoom() {
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Set<String> amenities = new HashSet<>();
        amenities.add("wifi");
        amenities.add("kitchen");
        amenities.add("air conditioner");
        amenities.add("tv");

        CreateRoomRequest createRoomRequest = CreateRoomRequest.builder()
                .hostId(1L)
                .placeType("house")
                .roomType("ENTIRE_PLACE")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .pricePerNight(700_000)
                .currency("KRW")
                .build();

        long roomId =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/rooms")
                        .then().log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class)
                        .getId();

        given().log().all()
                .port(port)
                .when().delete("/rooms/{roomId}", roomId)
                .then().log().all()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
