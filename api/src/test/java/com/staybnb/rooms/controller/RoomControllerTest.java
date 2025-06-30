package com.staybnb.rooms.controller;

import com.staybnb.common.jwt.JwtUtils;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.request.CreateRoomRequest;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.UpdateRoomRequest;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.dto.response.RoomResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomControllerTest {

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
                .currency("KRW")
                .basePrice(700_000)
                .build();

        RoomResponse response =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/host/rooms")
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
                .basePrice(createRoomRequest.getBasePrice()) // updated
                .currency(Currency.valueOf(createRoomRequest.getCurrency())) // updated
                .build();

        log.info("createRoomRequest: {}", createRoomRequest);
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
                .hostId(2L)
                .placeType("house")
                .roomType("ENTIRE_PLACE")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency("KRW")
                .build();

        long roomId =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/host/rooms")
                        .then().log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class)
                        .getId();

        UpdateRoomRequest updateRoomRequest = UpdateRoomRequest.builder()
                .maxNumberOfGuests(4)
                .basePrice(900_000)
                .currency("KRW")
                .build();

        RoomResponse response =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(updateRoomRequest)
                        .when().patch("/host/rooms/{roomId}", roomId)
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
                .basePrice(updateRoomRequest.getBasePrice()) // updated
                .currency(Currency.valueOf(updateRoomRequest.getCurrency())) // updated
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
                .hostId(2L)
                .placeType("house")
                .roomType("ENTIRE_PLACE")
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency("KRW")
                .build();

        long roomId =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/host/rooms")
                        .then().log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class)
                        .getId();

        given().log().all()
                .port(port)
                .when().delete("/host/rooms/{roomId}", roomId)
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
                .basePrice(700_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/host/rooms")
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
                .basePrice(700_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/host/rooms")
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
                .basePrice(700_000)
                .currency("KRW")
                .build();

        long roomId =
                given().log().all()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(createRoomRequest)
                        .when().post("/host/rooms")
                        .then().log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .extract().as(RoomResponse.class)
                        .getId();

        UpdateRoomRequest updateRoomRequest = UpdateRoomRequest.builder()
                .maxNumberOfGuests(4)
                .title(" ") // invalid @NotBlank
                .basePrice(900_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(updateRoomRequest)
                .when().patch("/host/rooms/{roomId}", roomId)
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
                .basePrice(700_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/host/rooms")
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
                .basePrice(700_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/host/rooms")
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
                .basePrice(700_000)
                .currency("unsupported") // unsupported Currency
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/host/rooms")
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
                .basePrice(700_000)
                .currency("KRW")
                .build();

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(createRoomRequest)
                .when().post("/host/rooms")
                .then().log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
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

    @Test
    public void updatePricing() {
        UpdatePricingRequest request = new UpdatePricingRequest(
                List.of(
                        new DateRangeRequest(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
                        new DateRangeRequest(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7))
                ),
                400000
        );

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/host/rooms/{roomId}/pricing", 1L)
                .then().log().all()
                .statusCode(HttpStatus.SC_OK);
    }

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

    @Test
    public void updateAvailability() {
        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest(
                List.of(
                        new DateRangeRequest(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
                        new DateRangeRequest(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7))
                ), true);

        given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/host/rooms/{roomId}/availability", 1L)
                .then().log().all()
                .statusCode(HttpStatus.SC_OK);
    }

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
