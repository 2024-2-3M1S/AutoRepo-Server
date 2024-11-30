package org.autorepo.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.entity.UserRole;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreateUser(String githubId, String githubToken) {
        return userRepository.findByGithubId(githubId).orElseGet(() -> {
            User newUser = User.builder()
                    .githubId(githubId)
                    .githubToken(githubToken)
                    .userRole(UserRole.USER)
                    .build();
            return userRepository.save(newUser);
        });
    }
}