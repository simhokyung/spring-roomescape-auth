package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReservationDao {

    private static final String RESERVATION_SELECT = """
            SELECT
                r.id as reservation_id,
                m.id as member_id,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date,
                rt.id as time_id,
                rt.start_at as time_value,
                th.id as theme_id,
                th.name as theme_name,
                th.description,
                th.thumbnail
            FROM reservation r
            INNER JOIN member m
            ON r.member_id = m.id
            INNER JOIN reservation_time rt
            ON r.time_id = rt.id
            INNER JOIN theme th
            ON r.theme_id = th.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("reservation_id"),
            new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_password")
            ),
            LocalDate.parse(resultSet.getString("date")),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    LocalTime.parse(resultSet.getString("time_value"))
            ),
            new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            )
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Reservation> findAll(int page, int size) {
        String sql = RESERVATION_SELECT + """
                ORDER BY r.date ASC, rt.start_at ASC, th.name ASC, m.name ASC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;

        return jdbcTemplate.query(sql, reservationRowMapper, size, offset);
    }

    public List<Reservation> findByName(String name, int page, int size) {
        String sql = RESERVATION_SELECT + """
                WHERE m.name = ?
                ORDER BY r.date ASC, rt.start_at ASC, th.name ASC, m.name ASC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;

        return jdbcTemplate.query(sql, reservationRowMapper, name, size, offset);
    }

    public List<Reservation> findByMemberId(Long memberId, int page, int size) {
        String sql = RESERVATION_SELECT + """
                WHERE m.id = ?
                ORDER BY r.date ASC, rt.start_at ASC, th.name ASC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;

        return jdbcTemplate.query(sql, reservationRowMapper, memberId, size, offset);
    }

    public Optional<Reservation> findById(long id) {
        String sql = RESERVATION_SELECT + """
                WHERE r.id = ?
                """;

        List<Reservation> result = jdbcTemplate.query(sql, reservationRowMapper, id);
        return result.stream().findFirst();
    }

    public Reservation save(Reservation reservation) {
        Long memberId = resolveMemberId(reservation);
        String sql = "INSERT INTO reservation (member_id, name, date, time_id, theme_id) VALUES(?,?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});

            preparedStatement.setLong(1, memberId);
            preparedStatement.setString(2, reservation.getName());
            preparedStatement.setString(3, reservation.getDate().toString());
            preparedStatement.setLong(4, reservation.getTimeId());
            preparedStatement.setLong(5, reservation.getThemeId());

            return preparedStatement;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return findById(id).orElseThrow();
    }

    public List<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                select time_id
                from reservation
                where date = ?
                and theme_id = ?
                """;

        return jdbcTemplate.query(sql,
                (resultSet, rowNum) -> resultSet.getLong("time_id"),
                date.toString(),
                themeId
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void updateDateAndTime(long id, LocalDate date, Long timeId) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, date.toString(), timeId, id);
    }

    public boolean existByTimeId(Long timeId) {
        String sql = "SELECT count(*) FROM reservation where time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    public boolean existByThemeId(Long themeId) {
        String sql = "SELECT count(*) FROM reservation where theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    public boolean existByDateAndTimeAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT count(*) FROM reservation
                where date = ?
                and time_id = ?
                and theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    private Long resolveMemberId(Reservation reservation) {
        if (reservation.getMemberId() != null) {
            return reservation.getMemberId();
        }

        return findMemberIdByName(reservation.getName())
                .orElseGet(() -> saveLegacyMember(reservation.getName()));
    }

    private Optional<Long> findMemberIdByName(String name) {
        String sql = "SELECT id FROM member WHERE name = ? LIMIT 1";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Long saveLegacyMember(String name) {
        String sql = "INSERT INTO member (name, email, password) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, name + "@reservation.local");
            preparedStatement.setString(3, "password");

            return preparedStatement;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }
}
