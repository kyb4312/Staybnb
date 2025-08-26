package com.staybnb.common.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutTokenService {

    public static final String BLACKLIST = "blacklist:";
    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;

    public void logout(String token) {
        String jti = jwtUtils.getJti(token);
        String username = jwtUtils.getUserName(token);
        long expirationTime = jwtUtils.getExpirationTimeMillis(token);

        if (jti != null) {
            redisTemplate.opsForValue().set(BLACKLIST + jti, username, expirationTime, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String jti = jwtUtils.getJti(token);
        if (jti == null) {
            return false;
        }
        return redisTemplate.hasKey(BLACKLIST + jti);
    }

}
