package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberDaoTest {

    @Autowired
    private MemberDao memberDao;

    @Test
    void 이메일로_회원을_조회한다() {
        Optional<Member> member = memberDao.findByEmail("brown@example.com");

        assertThat(member).isPresent();
        assertThat(member.get().getName()).isEqualTo("brown");
        assertThat(member.get().getEmail()).isEqualTo("brown@example.com");
    }

    @Test
    void 존재하지_않는_이메일이면_빈_값을_반환한다() {
        Optional<Member> member = memberDao.findByEmail("none@example.com");

        assertThat(member).isEmpty();
    }

    @Test
    void id로_회원을_조회한다() {
        Member member = memberDao.findByEmail("brown@example.com")
                .orElseThrow();

        Optional<Member> foundMember = memberDao.findById(member.getId());

        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo("brown@example.com");
    }
}
