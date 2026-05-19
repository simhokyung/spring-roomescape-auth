package roomescape.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.dto.ErrorResponse;

import java.io.IOException;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AuthenticationInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!requiresAuthentication(request)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            return true;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse("UNAUTHORIZED", "로그인이 필요합니다.")
        );
        return false;
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (HttpMethod.GET.matches(method) && uri.equals("/members/me")) {
            return true;
        }

        if (HttpMethod.POST.matches(method) && uri.equals("/reservations")) {
            return true;
        }

        return false;
    }
}
