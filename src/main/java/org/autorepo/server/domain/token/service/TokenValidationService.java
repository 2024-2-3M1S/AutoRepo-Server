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
            if (!tokenValidator.isTokenValid(user.getGithubToken())) {
                System.out.println("Token expired for user: " + user.getGithubId());
            }
        }
    }
}
