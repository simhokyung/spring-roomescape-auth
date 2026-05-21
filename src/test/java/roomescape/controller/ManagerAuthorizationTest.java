package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ManagerAuthorizationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        createTime("10:00");
        createTime("11:00");
        createTheme("horror");
    }

    @Test
    void 로그인하지_않으면_매니저_예약_목록을_조회할_수_없다() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(401)
                .body("code", is("UNAUTHORIZED"));
    }

    @Test
    void 매니저는_자기_매장_예약만_조회한다() {
        String brownSessionId = login("brown@example.com");

        Long myStoreReservationId = createReservationByStoreId(1L, "brown-store", "2030-08-05", 1L);
        Long otherStoreReservationId = createReservationByStoreId(2L, "coney-store", "2030-08-06", 2L);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", brownSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].id", is(myStoreReservationId.intValue()));
    }

    @Test
    void 매니저는_다른_매장_예약을_삭제할_수_없다() {
        String brownSessionId = login("brown@example.com");

        Long otherStoreReservationId = createReservationByStoreId(
                2L,
                "coney-store",
                "2030-08-05",
                1L
        );

        RestAssured.given().log().all()
                .cookie("JSESSIONID", brownSessionId)
                .when().delete("/admin/reservations/" + otherStoreReservationId)
                .then().log().all()
                .statusCode(403)
                .body("code", is("FORBIDDEN"));
    }

    @Test
    void 매니저는_자기_매장_예약을_삭제할_수_있다() {
        String brownSessionId = login("brown@example.com");

        Long myStoreReservationId = createReservationByStoreId(
                1L,
                "brown-store",
                "2030-08-05",
                1L
        );

        RestAssured.given().log().all()
                .cookie("JSESSIONID", brownSessionId)
                .when().delete("/admin/reservations/" + myStoreReservationId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 매니저는_다른_매장_예약을_변경할_수_없다() {
        String brownSessionId = login("brown@example.com");

        Long otherStoreReservationId = createReservationByStoreId(
                2L,
                "coney-store",
                "2030-08-05",
                1L
        );

        Map<String, Object> updateRequest = Map.of(
                "date", "2030-08-06",
                "timeId", 2
        );

        RestAssured.given().log().all()
                .cookie("JSESSIONID", brownSessionId)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when().patch("/admin/reservations/" + otherStoreReservationId)
                .then().log().all()
                .statusCode(403)
                .body("code", is("FORBIDDEN"));
    }

    @Test
    void 매니저는_자기_매장_예약을_변경할_수_있다() {
        String brownSessionId = login("brown@example.com");

        Long myStoreReservationId = createReservationByStoreId(
                1L,
                "brown-store",
                "2030-08-05",
                1L
        );

        Map<String, Object> updateRequest = Map.of(
                "date", "2030-08-06",
                "timeId", 2
        );

        RestAssured.given().log().all()
                .cookie("JSESSIONID", brownSessionId)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when().patch("/admin/reservations/" + myStoreReservationId)
                .then().log().all()
                .statusCode(200)
                .body("date", is("2030-08-06"))
                .body("time.id", is(2));
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

    private void createTime(String startAt) {
        Map<String, String> timeRequest = Map.of(
                "startAt", startAt
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    private void createTheme(String name) {
        Map<String, String> themeRequest = Map.of(
                "name", name,
                "description", "scary room",
                "thumbnail", "https://roomescape.com/" + name + ".png"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    private Long createReservationByStoreId(Long storeId, String name, String date, Long timeId) {
        jdbcTemplate.update(
                """
                INSERT INTO reservation (member_id, store_id, name, date, time_id, theme_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                1L,
                storeId,
                name,
                date,
                timeId,
                1L
        );

        return jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM reservation",
                Long.class
        );
    }
}
