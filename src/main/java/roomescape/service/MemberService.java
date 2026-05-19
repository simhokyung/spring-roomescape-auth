package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.exception.AuthenticationException;
import roomescape.exception.DuplicateResourceException;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member join(String name, String email, String password) {
        if (memberDao.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "DUPLICATE_MEMBER",
                    "이미 존재하는 이메일입니다."
            );
        }

        Member member = new Member(name, email, password);
        return memberDao.save(member);
    }

    public Member login(String email, String password) {
        Member member = memberDao.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!member.getPassword().equals(password)) {
            throw new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }
}
