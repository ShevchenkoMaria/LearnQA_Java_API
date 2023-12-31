package test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

public class AuthorizeVer3 extends BaseTestCase {
    String cookie;
    String header;
    int userIdAuth;
    @BeforeEach
    public void loginUser(){
        Map<String,String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");
        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();
        this.cookie = this.getCookie(responseGetAuth,"auth_sid");
        this.header = this.getHeader(responseGetAuth,"x-csrf-token");
        this.userIdAuth = this.getIntFromJson(responseGetAuth,"user_id");
    }

    @Test
    public void authTest(){
        Response responseCheckAuth = RestAssured
                .given()
                .header("x-csrf-token",this.header)
                .cookie("auth_sid", this.cookie)
                .get("https://playground.learnqa.ru/api/user/auth")
                .andReturn();
        Assertions.assertJsonByName(responseCheckAuth,"user_id", this.userIdAuth);
    }
    @ParameterizedTest
    @ValueSource(strings = {"cookies", "headers"})
    public void authTestNegative(String condition) throws IllegalAccessException {
        RequestSpecification spec = RestAssured.given();
        spec.baseUri("https://playground.learnqa.ru/api/user/auth");
        if(condition.equals("cookies")){
            spec.cookie("auth_sid",this.cookie);
        } else if (condition.equals("headers")){
            spec.header("x-csrf-token",this.header);
        } else {
            throw new IllegalAccessException("Condition value is known " + condition);
        }
        Response responseForCheck = spec.get().andReturn();
        Assertions.assertJsonByName(responseForCheck,"user_id", 0);
    }
}
