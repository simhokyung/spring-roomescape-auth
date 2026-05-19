package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
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

    public Member save(Member member) {
        String sql = """
              INSERT INTO member (name, email, password)
              VALUES (?, ?, ?)
              """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPassword());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return new Member(
                id,
                member.getName(),
                member.getEmail(),
                member.getPassword()
        );
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT count(*) FROM member WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

}
