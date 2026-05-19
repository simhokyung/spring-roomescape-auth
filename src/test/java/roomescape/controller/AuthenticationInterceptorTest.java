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

    @Test
    void 요청_name이_있어도_로그인_사용자로_예약을_생성한다() {
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
                "date", "2030-08-06",
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
                .body("name", is("brown"));
    }

    @Test
    void 로그인하지_않으면_내_예약을_조회할_수_없다() {
        RestAssured.given().log().all()
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(401)
                .body("code", is("UNAUTHORIZED"))
                .body("message", is("로그인이 필요합니다."));
    }

    @Test
    void 로그인하면_내_예약을_조회할_수_있다() {
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
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", sessionId)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("brown"));
    }

    @Test
    void 다른_회원의_예약은_취소할_수_없다() {
        String brownSessionId = login("brown@example.com");
        String coneySessionId = login("coney@example.com");

        Integer reservationId = createReservation(brownSessionId, "2030-08-05");

        RestAssured.given().log().all()
                .cookie("JSESSIONID", coneySessionId)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403)
                .body("code", is("FORBIDDEN"))
                .body("message", is("본인의 예약만 취소할 수 있습니다."));
    }

    @Test
    void 다른_회원의_예약은_변경할_수_없다() {
        String brownSessionId = login("brown@example.com");
        String coneySessionId = login("coney@example.com");

        Integer reservationId = createReservation(brownSessionId, "2030-08-05");

        Map<String, Object> updateRequest = Map.of(
                "date", "2030-08-06",
                "timeId", 1
        );

        RestAssured.given().log().all()
                .cookie("JSESSIONID", coneySessionId)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403)
                .body("code", is("FORBIDDEN"))
                .body("message", is("본인의 예약만 변경할 수 있습니다."));
    }

    private String login(String email) {
        Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", "password"
        );

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");
    }

    private Integer createReservation(String sessionId, String date) {
        Map<String, Object> reservationRequest = Map.of(
                "date", date,
                "timeId", 1,
                "themeId", 1
        );

        return RestAssured.given().log().all()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
