package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthenticationInterceptorTest {

    @Test
    void 로그인하지_않으면_예약을_생성할_수_없다() {
        Map<String, Object> request = Map.of(
                "name", "other",
                "date", "2030-08-05",
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401)
                .body("code", is("UNAUTHORIZED"))
                .body("message", is("로그인이 필요합니다."));
    }

    @Test
    void 로그인하지_않아도_예약_목록은_기존처럼_조회할_수_있다() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }
}
