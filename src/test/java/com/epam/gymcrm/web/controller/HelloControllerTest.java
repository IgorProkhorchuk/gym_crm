package com.epam.gymcrm.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;

class HelloControllerTest {

    @BeforeEach
    void setUp() {
        standaloneSetup(new HelloController());
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    @Test
    void helloWorldShouldReturnGreeting() {
        given()
                .when()
                .get("/v1/hello")
                .then()
                .statusCode(200)
                .body(equalTo("<h1>Hello World!</h1>"));
    }
}
