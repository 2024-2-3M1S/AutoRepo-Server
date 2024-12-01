package org.autorepo.server.domain.token.service;


import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GitHubTokenValidator {

    private final WebClient webClient = WebClient.create("https://api.github.com");

    /**
     * 로그인 AccessToken을 검증
     * @param accessToken GitHub OAuth 로그인 AccessToken
     * @return 유효한 토큰이면 true, 아니면 false
     */
    public boolean validateLoginAccessToken(String accessToken) {
        try {
            webClient.get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true; // 유효한 토큰
        } catch (Exception e) {
            return false; // 유효하지 않은 토큰
        }
    }

    /**
     * Personal Access Token(PAT)을 검증
     * @param personalAccessToken GitHub Personal Access Token
     * @return 유효한 토큰이면 true, 아니면 false
     */
    public boolean validatePersonalAccessToken(String personalAccessToken) {
        try {
            webClient.get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + personalAccessToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPersonalAccessToken(String token) {
        return token.startsWith("gho_") || token.startsWith("ghp_");
    }
}
