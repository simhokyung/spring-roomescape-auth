package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Manager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ManagerDaoTest {

    @Autowired
    private ManagerDao managerDao;

    @Test
    void 회원_id로_매니저를_조회한다(){
        Optional<Manager> manager = managerDao.findByMemberId(1L);

        assertThat(manager).isPresent();
        assertThat(manager.get().getMemberId()).isEqualTo(1L);
        assertThat(manager.get().getStoreId()).isEqualTo(1L);
    }

    @Test
    void 매니저가_아닌_회원이면_빈_값을_반환한다(){
        Optional<Manager> manager = managerDao.findByMemberId(999L);
        assertThat(manager).isEmpty();
    }
}
