package org.autorepo.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.entity.UserRole;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // GitHub의 email 필드를 가져오되, null이면 login으로 대체
        String email = oAuth2User.getAttribute("email");
        final String githubEmail = email != null ? email : oAuth2User.getAttribute("login");

        User user = userRepository.findByGithubId(githubEmail).orElseGet(() -> {
            User newUser = User.builder()
                    .githubId(githubEmail)
                    .userRole(UserRole.USER)
                    .build();
            return userRepository.save(newUser);
        });

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "id");
    }
}