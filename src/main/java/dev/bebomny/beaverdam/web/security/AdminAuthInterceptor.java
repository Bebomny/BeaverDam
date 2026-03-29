package dev.bebomny.beaverdam.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Value("${web.config.admin_email}")
    private String adminEmail;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean requiresAdmin = handlerMethod.getMethod().isAnnotationPresent(AdminOnly.class)
                || handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class);

        if (requiresAdmin) {
            String userEmail = request.getHeader("X-User-Email");

            if (userEmail == null || !userEmail.equalsIgnoreCase(adminEmail)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }

        return true;
    }
}
