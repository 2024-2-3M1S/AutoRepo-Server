package org.autorepo.server.global.common.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        // 토큰이 존재하는 경우 검증 시작
        if (token != null) {
            // 블랙리스트 확인
            if (isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("AccessToken has been blacklisted.");
                return;
            }

            // 토큰 검증
            if (jwtTokenProvider.isPersonalAccessToken(token)) {
                // Personal Access Token 검증 로직
                if (!jwtTokenProvider.validatePAT(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid Personal Access Token.");
                    return;
                }
                // PAT는 사용자 정보를 SecurityContext에 저장하지 않음 (GitHub API가 필요할 때 직접 호출)
            } else {
                // JWT 검증 및 사용자 정보 추출
                if (!jwtTokenProvider.validateJWT(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid JWT Token.");
                    return;
                }

                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }


    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isBlacklisted(String token) {
        String blacklistKey = "blacklist:" + token;
        Boolean hasKey = redisTemplate.hasKey(blacklistKey);
        return hasKey != null ? hasKey : false;
    }
}

