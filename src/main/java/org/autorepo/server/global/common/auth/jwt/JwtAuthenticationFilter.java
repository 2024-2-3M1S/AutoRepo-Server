package org.autorepo.server.global.common.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            // 블랙리스트 확인
            if (isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("AccessToken has been blacklisted.");
                return;
            }

            try {
                if (jwtTokenProvider.isPersonalAccessToken(token)) {
                    String githubLogin = fetchUserIdFromGitHub(token); // GitHub 'login' 정보 가져오기
                    if (githubLogin == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid Personal Access Token: User login not found.");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            githubLogin, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("PAT authenticated: Principal set to GitHub login = {}", githubLogin);
                } else {
                    if (!jwtTokenProvider.validateJWT(token)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid JWT Token.");
                        return;
                    }
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("JWT authenticated: Principal set to userId = " + userId);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Token validation failed: " + e.getMessage());
                return;
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

    private String fetchUserIdFromGitHub(String token) {
        String apiUrl = "https://api.github.com/user";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("login")) {
                return (String) body.get("login"); // GitHub의 'login' 정보 반환
            }
        } catch (Exception e) {
            log.error("Error fetching user login from GitHub", e);
        }
        return null; // GitHub에서 정보를 가져오지 못하면 null 반환
    }

}

