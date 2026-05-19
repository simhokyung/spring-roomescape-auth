package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthenticationInterceptorTest {

    @BeforeEach
    void setUp() {
        Map<String, String> timeRequest = Map.of(
                "startAt", "10:00"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> themeRequest = Map.of(
                "name", "horror",
                "description", "scary room",
                "thumbnail", "https://roomescape.com/horror.png"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

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

    @Test
    void 로그인하면_예약을_생성할_수_있고_요청_name은_무시된다() {
        Map<String, String> loginRequest = Map.of(
                "email", "brown@example.com",
                "password", "password"
        );

        String sessionId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");

        Map<String, Object> reservationRequest = Map.of(
                "name", "other",
                "date", "2030-08-05",
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("brown"))
                .body("date", is("2030-08-05"))
                .body("time.id", is(1))
                .body("theme.id", is(1));
    }
}
