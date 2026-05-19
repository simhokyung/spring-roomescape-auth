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
class MemberControllerTest {

    @Test
    void 회원가입에_성공한다() {
        Map<String, String> request = Map.of(
                "name", "david",
                "email", "david@example.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("david"))
                .body("email", is("david@example.com"));
    }

    @Test
    void 중복된_이메일로_회원가입할_수_없다() {
        Map<String, String> request = Map.of(
                "name", "brown2",
                "email", "brown@example.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DUPLICATE_MEMBER"))
                .body("message", is("이미 존재하는 이메일입니다."));
    }

    @Test
    void 이름이_없으면_회원가입에_실패한다() {
        Map<String, String> request = Map.of(
                "name", "",
                "email", "david@example.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"));
    }
}
