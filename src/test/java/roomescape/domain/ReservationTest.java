package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

class ReservationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a", "pobizoninavy", " "})
    @DisplayName("이름은 2글자 이상 10글자 이내여야 한다.")
    void 불가한_이름(String name) {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.parse("10:00"));
        Theme theme = new Theme("공포", "무서움", "https://roomescape.com");

        //when & then
        assertThatThrownBy(() -> new Reservation(name, LocalDate.parse("2030-04-10"), reservationTime, theme))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("이름 형식");
    }

    @Test
    void 회원_기반으로_예약을_생성한다() {
        Member member = new Member(1L, "brown", "brown@example.com", "password");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "horror", "scary room", "thumbnail");

        Reservation reservation = Reservation.create(
                member,
                LocalDate.of(2030, 8, 5),
                time,
                theme,
                LocalDateTime.of(2026, 5, 19, 12, 0)
        );

        assertThat(reservation.getMember()).isEqualTo(member);
        assertThat(reservation.getMemberId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("brown");
    }
}
