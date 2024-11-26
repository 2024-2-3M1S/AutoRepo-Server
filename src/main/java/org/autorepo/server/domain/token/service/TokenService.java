package org.autorepo.server.domain.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autorepo.server.domain.token.repository.TokenRepository;
import org.autorepo.server.global.common.auth.jwt.JwtTokenProvider;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.RefreshTokenNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    // RefreshToken 저장
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = String.valueOf(userId);
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofHours(24) // RefreshToken 24시간 만료
        );
    }

    // RefreshToken 조회
    public String findRefreshToken(Long userId) {
        String token = redisTemplate.opsForValue().get(String.valueOf(userId));
        if (token == null) {
            throw new RefreshTokenNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        return token;
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(Long userId) {
        String key = String.valueOf(userId);
        redisTemplate.delete(key);
        log.info("RefreshToken for userId {} deleted successfully.", userId);
    }

    // 로그아웃 처리
    public void logout(Long userId) {
        deleteRefreshToken(userId);

        // AccessToken 무효화 (Redis 블랙리스트 추가)
        String blacklistKey = "blacklist:" + userId;
        redisTemplate.opsForValue().set(blacklistKey, "true", Duration.ofHours(1)); // 1시간 블랙리스트 유지

        log.info("User with ID {} logged out. RefreshToken deleted and AccessToken blacklisted.", userId);
    }

    // AccessToken 생성
    public String generateAccessToken(Long userId) {
        return jwtTokenProvider.issueAccessToken(userId);
    }

    // RefreshToken으로 userId 찾기
    public Long findUserIdByRefreshToken(String refreshToken) {
        // Redis에서 모든 키 가져오기
        Set<String> keys = redisTemplate.keys("*");
        for (String key : keys) {
            String token = redisTemplate.opsForValue().get(key);
            if (refreshToken.equals(token)) {
                return Long.valueOf(key); // Redis 키(userId)를 반환
            }
        }
        throw new RefreshTokenNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}

