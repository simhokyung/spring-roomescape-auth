package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Manager;

import java.util.List;
import java.util.Optional;

@Repository
public class ManagerDao {

    private final JdbcTemplate jdbcTemplate;

    public ManagerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Manager> findByMemberId(Long memberId) {
        String sql = """
                  SELECT id, member_id, store_id
                  FROM manager
                  WHERE member_id = ?
                  """;

        List<Manager> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Manager(
                        rs.getLong("id"),
                        rs.getLong("member_id"),
                        rs.getLong("store_id")
                ),
                memberId
        );

        return result.stream().findFirst();
    }
}
