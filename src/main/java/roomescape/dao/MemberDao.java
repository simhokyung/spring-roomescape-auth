package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberDao {

    private final JdbcTemplate jdbcTemplate;

    public MemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Member> findById(Long id) {
        String sql = """
                  SELECT id, name, email, password
                  FROM member
                  WHERE id = ?
                  """;

        List<Member> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Member(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                ),
                id
        );

        return result.stream().findFirst();
    }

    public Optional<Member> findByEmail(String email) {
        String sql = """
                  SELECT id, name, email, password
                  FROM member
                  WHERE email = ?
                  """;

        List<Member> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Member(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                ),
                email
        );

        return result.stream().findFirst();
    }
}
