package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessionAuthenticationExtractor {

    public Optional<LoginMember> extract(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }

        Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember instanceof LoginMember member) {
            return Optional.of(member);
        }

        return Optional.empty();
    }
}
