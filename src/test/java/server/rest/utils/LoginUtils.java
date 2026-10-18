package server.rest.utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import server.rest.config.BaseTest;

import static io.restassured.RestAssured.given;

public class LoginUtils extends BaseTest {

    public String getBodyLogin(String email, String password) {
        return "{\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"password\": \"" + password + "\"\n" +
                "}";
    }

    @Step("Realizando login na API ServeRest")
    public Response realizaLogin(String pathLogin, String bodyLogin){
        return given().
                body(bodyLogin).
                log().all().
        when().
                log().all().
                post(pathLogin);
    }

}
