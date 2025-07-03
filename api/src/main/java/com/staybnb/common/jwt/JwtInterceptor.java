package com.staybnb.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staybnb.common.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

import static com.staybnb.common.constant.RequestAttributes.USER_ID;
import static com.staybnb.common.constant.RequestAttributes.USER_NAME;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtil;
    private final LogoutTokenService logoutTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, "ERROR", "Missing or invalid Authorization header");
            return false;
        }
        String token = authorization.substring(7);

        if (logoutTokenService.isTokenBlacklisted(token)) {
            sendErrorResponse(response, "ERROR", "Token is blacklisted (logged out)");
            return false;
        }

        if (jwtUtil.validateToken(token)) {
            request.setAttribute(USER_ID, Long.parseLong(jwtUtil.getUserId(token)));
            request.setAttribute(USER_NAME, jwtUtil.getUserName(token));
            return true;
        } else {
            sendErrorResponse(response, "ERROR", "Invalid token");
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(new ExceptionResponse(code, message)));
    }
}
