package com.example.app.ws.restassuredtest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsersWebServiceEndpointTest {

    private final String CONTEXT_PATH = "/mobile-app-ws";
    private final String EMAIL_ADDRESS = "aidas.vir@gmail.com";
    private final String PASSWORD = "123";
    private final String JSON = "application/json";
    private static String authorizationHeader;
    private static String userId;
    private static List<Map<String, String>> addresses;


    @BeforeEach
    void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;

    }

    @Test
    @Order(1)
    void userLogin() {

        Map<String, String> loginDetails = new HashMap<>();
        loginDetails.put("email", EMAIL_ADDRESS);
        loginDetails.put("password", PASSWORD);

        Response response = given()
            .contentType(JSON)
            .accept(JSON)
            .body(loginDetails)
            .when()
            .post(CONTEXT_PATH + "/users/login")
            .then()
            .statusCode(200)
            .extract().response();

        authorizationHeader = response.header("Authorization");
        userId = response.header("UserID");

        assertNotNull(authorizationHeader);
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    void getUserDetails() {
        Response response = given()
            .pathParams("userId", userId)
            .header("Authorization", authorizationHeader)
            .accept(JSON)
            .when()
            .get(CONTEXT_PATH + "/users/{userId}")
            .then()
            .statusCode(200)
            .contentType(JSON)
            .extract()
            .response();

        String userPublicId = response.jsonPath().getString("userId");
        String userEmail = response.jsonPath().getString("email");
        String firstName = response.jsonPath().getString("firstName");
        String lastName = response.jsonPath().getString("lastName");

        addresses = response.jsonPath().getList("addresses");
        String addressId = addresses.get(0).get("addressId");

        assertNotNull(userPublicId);
        assertNotNull(userEmail);
        assertNotNull(firstName);
        assertNotNull(lastName);

        assertEquals(EMAIL_ADDRESS, userEmail);
        assertEquals(2, addresses.size());
        assertEquals(30, addressId.length());
    }

    @Test
    @Order(3)
    void updateUserDetails() {

        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("firstName", "Test11");
        userDetails.put("lastName", "Virvicius");

        Response response = given()
            .contentType(JSON)
            .accept(JSON)
            .header("Authorization", authorizationHeader)
            .pathParams("userId", userId)
            .body(userDetails)
            .when()
            .put(CONTEXT_PATH + "/users/{userId}")
            .then()
            .statusCode(200)
            .contentType(JSON)
            .extract()
            .response();

        String firstName = response.jsonPath().getString("firstName");
        String lastName = response.jsonPath().getString("lastName");

        List<Map<String, String>> storedAddresses = response.jsonPath().getList("addresses");

        assertEquals("Test11", firstName);
        assertEquals("Virvicius", lastName);
        assertNotNull(storedAddresses);
        assertEquals(addresses.size(), storedAddresses.size());
        assertEquals(addresses.get(0).get("streetName"), storedAddresses.get(0).get("streetName"));

    }

    @Test
//    @Disabled
    @Order(4)
    void deleteUserDetails() {
        Response response = given()
            .accept(JSON)
            .header("Authorization", authorizationHeader)
            .pathParams("userId", userId)
            .when()
            .delete(CONTEXT_PATH + "/users/{userId}")
            .then()
            .statusCode(200)
            .contentType(JSON)
            .extract()
            .response();

        String operationResult = response.jsonPath().getString("operationResult");
        assertEquals("SUCCESS", operationResult);
    }
}
