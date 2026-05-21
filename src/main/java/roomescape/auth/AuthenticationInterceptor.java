package roomescape.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final SessionAuthenticationExtractor sessionAuthenticationExtractor;

    public AuthenticationInterceptor(
            ObjectMapper objectMapper,
            SessionAuthenticationExtractor sessionAuthenticationExtractor
    ) {
        this.objectMapper = objectMapper;
        this.sessionAuthenticationExtractor = sessionAuthenticationExtractor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!requiresAuthentication(request)) {
            return true;
        }

        if (sessionAuthenticationExtractor.extract(request).isPresent()) {
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

        if (HttpMethod.GET.matches(method) && uri.equals("/reservations/mine")) {
            return true;
        }

        if (HttpMethod.GET.matches(method) && uri.equals("/admin/reservations")) {
            return true;
        }

        if (HttpMethod.POST.matches(method) && uri.equals("/reservations")) {
            return true;
        }

        if (HttpMethod.PATCH.matches(method) && uri.startsWith("/reservations/")) {
            return true;
        }

        if (HttpMethod.DELETE.matches(method) && uri.startsWith("/reservations/")) {
            return true;
        }

        if (HttpMethod.DELETE.matches(method) && uri.startsWith("/admin/reservations/")) {
            return true;
        }

        return false;
    }
}
