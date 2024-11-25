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

        // GitHub AccessToken 가져오기
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // 사용자 찾기 또는 생성 및 업데이트
        User user = userRepository.findByGithubId(githubEmail).map(existingUser -> {
            // 기존 사용자일 경우 토큰 업데이트
            existingUser.setGithubToken(accessToken);
            return userRepository.save(existingUser);
        }).orElseGet(() -> {
            // 새로운 사용자일 경우 생성
            User newUser = User.builder()
                    .githubId(githubEmail)
                    .githubToken(accessToken)
                    .userRole(UserRole.USER)
                    .build();
            return userRepository.save(newUser);
        });

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "id");
    }
}