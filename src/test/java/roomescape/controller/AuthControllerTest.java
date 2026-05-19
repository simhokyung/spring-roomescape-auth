package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerTest {

    @Test
    void 로그인에_성공한다() {
        Map<String, String> request = Map.of(
                "email", "brown@example.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .cookie("JSESSIONID", notNullValue())
                .body("name", is("brown"))
                .body("email", is("brown@example.com"));
    }

    @Test
    void 비밀번호가_틀리면_로그인에_실패한다() {
        Map<String, String> request = Map.of(
                "email", "brown@example.com",
                "password", "wrong-password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(401)
                .body("code", is("AUTHENTICATION_FAILED"))
                .body("message", is("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 존재하지_않는_이메일이면_로그인에_실패한다() {
        Map<String, String> request = Map.of(
                "email", "none@example.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(401)
                .body("code", is("AUTHENTICATION_FAILED"))
                .body("message", is("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 이메일이_없으면_로그인에_실패한다() {
        Map<String, String> request = Map.of(
                "email", "",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"));
    }

    @Test
    void 로그인하지_않으면_내_정보를_조회할_수_없다() {
        RestAssured.given().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401)
                .body("code", is("UNAUTHORIZED"))
                .body("message", is("로그인이 필요합니다."));
    }

    @Test
    void 로그인하면_내_정보를_조회할_수_있다() {
        Map<String, String> request = Map.of(
                "email", "brown@example.com",
                "password", "password"
        );

        String sessionId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");

        RestAssured.given().log().all()
                .cookie("JSESSIONID", sessionId)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("brown"))
                .body("email", is("brown@example.com"));
    }
}
