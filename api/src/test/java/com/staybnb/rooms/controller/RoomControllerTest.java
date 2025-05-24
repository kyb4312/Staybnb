package com.staybnb.rooms.controller;

import com.staybnb.domain.Address;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomControllerTest {

    @LocalServerPort
    int port;

    @Test
    public void testGetRoom() {
        long roomId = 1L;
        RoomResponse roomResponse =
                given().log().all()
                        .port(port)
                        .when().get("/rooms/{roomId}", roomId)
                        .then()
                        .statusCode(200)
                        .extract().as(RoomResponse.class);

        assertThat(roomResponse.getId(), equalTo(roomId));
    }

    @Test
    public void testGetRooms() {
        given().log().all()
                .port(port)
                .when().get("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON);
    }

    @Test
    public void testCreateRoom() {
        CreateRoomRequest createRoomRequest = getDummyCreateRoomRequest();

        RoomResponse response =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/rooms")
                        .then()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class);

        assertThat(response.getId(), notNullValue());
        assertThat(response.getHostId(), equalTo(createRoomRequest.getHostId()));
        assertThat(response.getTitle(), equalTo(createRoomRequest.getTitle()));
    }

    @Test
    public void testUpdateRoom() {
        long roomId = 1L;
        UpdateRoomRequest updateRoomRequest = getDummyUpdateRoomRequest();

        RoomResponse response =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(updateRoomRequest)
                        .when().patch("/rooms/{roomId}", roomId)
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract().as(RoomResponse.class);

        assertThat(response.getId(), equalTo(roomId));
        assertThat(response.getMaxNumberOfGuests(), equalTo(updateRoomRequest.getMaxNumberOfGuests()));
        assertThat(response.getPricePerNight(), equalTo(updateRoomRequest.getPricePerNight()));
    }

    @Test
    public void testDeleteRoom() {
        long roomId = 1L;
        given().log().all()
                .port(port)
                .when().delete("/rooms/{roomId}", roomId)
                .then().log().all()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    // dummy response data for api test
    private CreateRoomRequest getDummyCreateRoomRequest() {
        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        List<String> amenities = new ArrayList<>();
        amenities.add("wifi");
        amenities.add("air_conditioner");
        amenities.add("tv");
        amenities.add("kitchen");

        return CreateRoomRequest.builder()
                .hostId(1L)
                .placeType("building")
                .roomType("entirePlace")
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
    }

    // dummy response data for api test
    private UpdateRoomRequest getDummyUpdateRoomRequest() {
        return UpdateRoomRequest.builder()
                .maxNumberOfGuests(4)
                .pricePerNight(900_000)
                .currency("KRW")
                .build();
    }
}
