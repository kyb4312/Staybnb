package com.staybnb.users.controller;

import com.staybnb.AbstractIntegrationTest;
import com.staybnb.common.auth.jwt.JwtUtils;
import com.staybnb.users.dto.request.LoginRequest;
import com.staybnb.users.dto.request.SignupRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

public class UserControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    JwtUtils jwtUtils;

    @BeforeEach
    void resetRestAssured() {
        RestAssured.reset();
    }


    @Nested
    @DisplayName("Login Tests")
    class Login {

        @Test
        @DisplayName("[성공] 로그인 성공")
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
                    .statusCode(HttpStatus.SC_OK);
        }

        @Test
        @DisplayName("[실패] 탈퇴한 회원 정보로 로그인 시도 시 실패")
        void loginFail() {
            // 탈퇴한 회원 (isDeleted = true) 로그인 시도
            String email = "guest4@gmail.com";
            String password = "password";

            LoginRequest request = new LoginRequest(email, password);

            given().log().all()
                    .port(port)
                    .body(request)
                    .contentType(ContentType.JSON)
                    .when().post("/users/login")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class Logout {

        @Test
        @DisplayName("[성공] 로그아웃 성공")
        void logout() {
            String token = "Bearer " + jwtUtils.generateToken("3", "guest2");

            given().log().all()
                    .port(port)
                    .header("Authorization", token)
                    .when().post("/users/logout")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        }

        @Test
        @DisplayName("[실패] 로그아웃 후 만료된 토큰으로 로그아웃 재시도 시 예외")
        void logoutFailWithOutAuth() {
            String token = "Bearer " + jwtUtils.generateToken("3", "guest2");

            given().log().all()
                    .port(port)
                    .header("Authorization", token)
                    .when().post("/users/logout")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_NO_CONTENT);

            given().log().all()
                    .port(port)
                    .header("Authorization", token)
                    .when().post("/users/logout")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("SignUp Tests")
    class SignUp {

        @Test
        @DisplayName("[성공] 회원 가입 성공")
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
                    .statusCode(HttpStatus.SC_CREATED);
        }

        @Test
        @DisplayName("[실패] 이미 존재하는 이메일로 회원 가입 시도 시 예외")
        void signupFail() {
            // 이미 존재하는 이메일로 회원 가입 시도
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
                    .statusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DeleteAccount Tests")
    class DeleteAccount {

        @Test
        @DisplayName("[성공] 회원 탈퇴 성공")
        void loginAndDeleteAccount() {
            given().log().all()
                    .port(port)
                    .header("Authorization", "Bearer " + jwtUtils.generateToken("3", "guest2"))
                    .when().delete("/users/delete")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        }

        @Test
        @DisplayName("[실패] 토큰 없이 회원 탈퇴 시도 시 예외")
        void deleteAccountFailWithoutAuth() {
            given().log().all()
                    .port(port)
                    .auth().none()
                    .when().delete("/users/delete")
                    .then().log().all()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED);
        }
    }
}