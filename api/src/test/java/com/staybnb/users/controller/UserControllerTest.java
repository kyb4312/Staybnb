package com.staybnb.users.controller;

import com.staybnb.common.jwt.JwtUtils;
import com.staybnb.users.dto.request.LoginRequest;
import com.staybnb.users.dto.request.SignupRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    JwtUtils jwtUtils;

    @BeforeEach
    void resetRestAssured() {
        RestAssured.reset();
    }

    @Test
    void login() {
        String email = "admin@gmail.com";
        String password = "password";

        LoginRequest request = new LoginRequest(email, password);

        given().log().all()
                .port(port)
                .body(request)
                .contentType(ContentType.JSON)
                .when().post("/users/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void signup() {
        String email = "guest3@gmail.com";
        String password = "password";
        String name = "guest3";

        SignupRequest request = new SignupRequest(email, password, name);

        given().log().all()
                .port(port)
                .body(request)
                .contentType(ContentType.JSON)
                .when().post("/users/signup")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void signupFail() {
        String email = "guest1@gmail.com";
        String password = "password";
        String name = "guest1";

        SignupRequest request = new SignupRequest(email, password, name);

        given().log().all()
                .port(port)
                .body(request)
                .contentType(ContentType.JSON)
                .when().post("/users/signup")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void loginAndDeleteAccount() {
        given().log().all()
                .port(port)
                .header("Authorization", "Bearer " + jwtUtils.generateToken("3", "guest2"))
                .when().delete("/users/delete")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void deleteAccountFailWithoutAuth() {
        given().log().all()
                .port(port)
                .auth().none()
                .when().delete("/users/delete")
                .then().log().all()
                .statusCode(401);
    }

}