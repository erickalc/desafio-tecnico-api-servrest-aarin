package server.rest.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    @BeforeAll
    public static void setup() {
        RestAssured.reset();

        RestAssured.baseURI = "https://serverest.dev";

        RestAssured.requestSpecification = new RequestSpecBuilder().
                setContentType(ContentType.JSON).
                addFilter(new AllureRestAssured()).
                build();
    }

}
