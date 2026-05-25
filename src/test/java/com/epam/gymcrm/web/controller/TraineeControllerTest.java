package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private static final String TOKEN = "token";
    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "password";

    @Mock
    private GymFacade gymFacade;

    @Mock
    private FakeTokenService fakeTokenService;

    @BeforeEach
    void setUp() {
        standaloneSetup(new TraineeController(gymFacade, fakeTokenService), new RestExceptionHandler());
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    @Test
    void createTraineeShouldReturnCreatedCredentials() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                "John",
                "Doe",
                LocalDate.of(1995, 1, 10),
                "Main Street, 123"
        );
        UsernamePasswordResponse response = new UsernamePasswordResponse(USERNAME, PASSWORD);
        when(gymFacade.createTrainee(request)).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "dateOfBirth": "1995-01-10",
                          "address": "Main Street, 123"
                        }
                        """)
                .when()
                .post("/v1/trainees")
                .then()
                .statusCode(201)
                .body("username", equalTo(USERNAME))
                .body("password", equalTo(PASSWORD));

        verify(gymFacade).createTrainee(request);
    }

    @Test
    void createTraineeShouldReturnBadRequestWhenRequestIsInvalid() {
        CreateTraineeRequest request = new CreateTraineeRequest("", "Doe", null, null);
        when(gymFacade.createTrainee(request))
                .thenThrow(new IllegalArgumentException("First name must not be blank"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "",
                          "lastName": "Doe"
                        }
                        """)
                .when()
                .post("/v1/trainees")
                .then()
                .statusCode(400)
                .body("message", equalTo("First name must not be blank"));

        verify(gymFacade).createTrainee(request);
    }

    @Test
    void getTraineeProfileShouldReturnProfileForTraineeToken() {
        AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
        AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
        TraineeProfileResponse response = new TraineeProfileResponse(
                USERNAME,
                "John",
                "Doe",
                true,
                LocalDate.of(1995, 1, 10),
                "Main Street, 123",
                List.of()
        );
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
        when(gymFacade.getTraineeProfile(request)).thenReturn(response);

        given()
                .header("X-Auth-Token", TOKEN)
                .when()
                .get("/v1/trainees/profile")
                .then()
                .statusCode(200)
                .body("username", equalTo(USERNAME))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("active", equalTo(true))
                .body("address", equalTo("Main Street, 123"));

        verify(fakeTokenService).getUserByToken(TOKEN);
        verify(gymFacade).getTraineeProfile(request);
    }

    @Test
    void getTraineeProfileShouldRejectTrainerToken() {
        AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

        given()
                .header("X-Auth-Token", TOKEN)
                .when()
                .get("/v1/trainees/profile")
                .then()
                .statusCode(401)
                .body("message", equalTo("Access denied"));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void getTraineeProfileShouldRejectInvalidToken() {
        when(fakeTokenService.getUserByToken("invalid-token"))
                .thenThrow(new AuthenticationException("Invalid authentication token"));

        given()
                .header("X-Auth-Token", "invalid-token")
                .when()
                .get("/v1/trainees/profile")
                .then()
                .statusCode(401)
                .body("message", equalTo("Invalid authentication token"));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeProfileShouldReturnUpdatedProfile() {
        AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                USERNAME,
                PASSWORD,
                "Johnny",
                "Doe",
                LocalDate.of(1996, 2, 20),
                "Updated Street, 7",
                false
        );
        TraineeProfileResponse response = new TraineeProfileResponse(
                USERNAME,
                "Johnny",
                "Doe",
                false,
                LocalDate.of(1996, 2, 20),
                "Updated Street, 7",
                List.of()
        );
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
        when(gymFacade.updateTrainee(request)).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .header("X-Auth-Token", TOKEN)
                .body("""
                        {
                          "username": "John.Doe",
                          "firstName": "Johnny",
                          "lastName": "Doe",
                          "dateOfBirth": "1996-02-20",
                          "address": "Updated Street, 7",
                          "active": false
                        }
                        """)
                .when()
                .put("/v1/trainees/profile")
                .then()
                .statusCode(200)
                .body("username", equalTo(USERNAME))
                .body("firstName", equalTo("Johnny"))
                .body("lastName", equalTo("Doe"))
                .body("active", equalTo(false))
                .body("dateOfBirth", equalTo(List.of(1996, 2, 20)))
                .body("address", equalTo("Updated Street, 7"));

        verify(fakeTokenService).getUserByToken(TOKEN);
        verify(gymFacade).updateTrainee(request);
    }

    @Test
    void updateTraineeProfileShouldRejectTrainerToken() {
        AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

        given()
                .contentType(ContentType.JSON)
                .header("X-Auth-Token", TOKEN)
                .body("""
                        {
                          "username": "Mike.Stone",
                          "firstName": "Mike",
                          "lastName": "Stone",
                          "dateOfBirth": "1996-02-20",
                          "address": "Updated Street, 7",
                          "active": true
                        }
                        """)
                .when()
                .put("/v1/trainees/profile")
                .then()
                .statusCode(401)
                .body("message", equalTo("Access denied"));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeProfileShouldRejectAnotherUsername() {
        AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

        given()
                .contentType(ContentType.JSON)
                .header("X-Auth-Token", TOKEN)
                .body("""
                        {
                          "username": "Another.User",
                          "firstName": "Johnny",
                          "lastName": "Doe",
                          "dateOfBirth": "1996-02-20",
                          "address": "Updated Street, 7",
                          "active": true
                        }
                        """)
                .when()
                .put("/v1/trainees/profile")
                .then()
                .statusCode(401)
                .body("message", equalTo("Access denied"));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void changePasswordShouldReturnOkForTraineeToken() {
        AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, "new-password");
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

        given()
                .contentType(ContentType.JSON)
                .header("X-Auth-Token", TOKEN)
                .body("""
                        {
                          "username": "John.Doe",
                          "oldPassword": "password",
                          "newPassword": "new-password"
                        }
                        """)
                .when()
                .put("/v1/trainees/password")
                .then()
                .statusCode(200);

        verify(gymFacade).changeTraineePassword(request);
        verify(fakeTokenService).updatePassword(TOKEN, "new-password");
    }

    @Test
    void changePasswordShouldRejectTrainerToken() {
        AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

        given()
                .contentType(ContentType.JSON)
                .header("X-Auth-Token", TOKEN)
                .body("""
                        {
                          "username": "Mike.Stone",
                          "oldPassword": "password",
                          "newPassword": "new-password"
                        }
                        """)
                .when()
                .put("/v1/trainees/password")
                .then()
                .statusCode(401)
                .body("message", equalTo("Access denied"));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void changePasswordShouldRejectAnotherUsername() {
        AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
        when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

        given()
                .contentType(ContentType.JSON)
                .header("X-Auth-Token", TOKEN)
                .body("""
                        {
                          "username": "Another.User",
                          "oldPassword": "password",
                          "newPassword": "new-password"
                        }
                        """)
                .when()
                .put("/v1/trainees/password")
                .then()
                .statusCode(401)
                .body("message", equalTo("Access denied"));

        verifyNoInteractions(gymFacade);
    }
}
