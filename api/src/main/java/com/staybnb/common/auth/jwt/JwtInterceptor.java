package com.staybnb.common.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staybnb.common.exception.ExceptionResponse;
import com.staybnb.common.auth.dto.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

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
            sendErrorResponse(response, "A001", "Missing or invalid Authorization header");
            return false;
        }
        String token = authorization.substring(7);

        if (logoutTokenService.isTokenBlacklisted(token)) {
            sendErrorResponse(response, "A002", "Token is blacklisted (logged out)");
            return false;
        }

        if (jwtUtil.validateToken(token)) {
            LoginUser loginUser = new LoginUser(Long.parseLong(jwtUtil.getUserId(token)), jwtUtil.getUserName(token));
            request.setAttribute("LOGIN_USER", loginUser);
            return true;
        } else {
            sendErrorResponse(response, "A003", "Invalid token");
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
