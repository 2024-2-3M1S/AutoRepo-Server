package org.autorepo.server.domain.token.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final UserRepository userRepository;
    private final GitHubTokenValidator tokenValidator;

    @Scheduled(fixedRate = 86400000) // 매일 한 번 실행
    public void validateTokens() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String token = user.getGithubToken();

            if (isPersonalAccessToken(token)) {
                if (!tokenValidator.validatePersonalAccessToken(token)) {
                    System.out.println("Personal Access Token expired for user: " + user.getGithubId());
                }
            } else {
                if (!tokenValidator.validateLoginAccessToken(token)) {
                    System.out.println("Login Access Token expired for user: " + user.getGithubId());
                }
            }
        }
    }

    /**
     * 토큰이 Personal Access Token인지 확인
     * @param token 검증할 토큰
     * @return PAT이면 true, 아니면 false
     */
    private boolean isPersonalAccessToken(String token) {
        return token.startsWith("gho_") || token.startsWith("ghp_");
    }
}
