package com.staybnb.rooms.controller;

import com.staybnb.rooms.domain.vo.Address;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.response.RoomResponse;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomControllerTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("staybnb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // jpa 에서 ddl 실행 방지
        registry.add("spring.sql.init.mode", () -> "always"); // schema.sql, data.sql 스크립트 실행
    }

    @LocalServerPort
    int port;

    @Test
    @DisplayName("GetOne: 정상")
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
    @DisplayName("GetAll: 정상")
    public void testGetRooms() {
        testCreateRoom();
        given().log().all()
                .port(port)
                .when().get("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Create: 정상")
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
    @DisplayName("Update: 정상")
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
    @DisplayName("Delete: 정상")
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

    @Test
    @DisplayName("Create: placeType이 공백인 경우")
    public void testInvalidPlaceTypeCreateRoom() {
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
                .placeType(" ") // invalid @NotBlank
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

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Create: Country가 공백인 경우")
    public void testInvalidCountryCreateRoom() {
        Address address = Address.builder()
                .country("house")
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
                .placeType(" ") // invalid @NotBlank
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

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Update: Title이 공백인 경우")
    public void testInvalidTitleUpdateRoom() {
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
                .title(" ") // invalid @NotBlank
                .pricePerNight(900_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(updateRoomRequest)
                .when().patch("/rooms/{roomId}", roomId)
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
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

    @Test
    @DisplayName("Create: 지원하지 않는 PlaceType인 경우")
    public void testUnsupportedPlaceTypeCreateRoom() {
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
                .placeType("unsupported") // unsupported PlaceType
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

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Create: 지원하지 않는 Amenity인 경우")
    public void testUnsupportedAmenityCreateRoom() {
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
        amenities.add("unsupported"); // unsupported Amenity

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

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Create: 지원하지 않는 Currency인 경우")
    public void testUnsupportedCurrencyCreateRoom() {
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
                .currency("unsupported") // unsupported Currency
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Create: 지원하지 않는 RoomType인 경우")
    public void testUnsupportedRoomTypeCreateRoom() {
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
                .roomType("unsupported")
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

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("GetOne: 존재하지 않는 숙소 검색")
    public void testNonExistentRoomIdGetRoom() {
        testCreateRoom();

        long roomId = -1L;
        given().log().all()
                .port(port)
                .when().get("/rooms/{roomId}", roomId)
                .then().log().all()
                .statusCode(400);
    }
}
