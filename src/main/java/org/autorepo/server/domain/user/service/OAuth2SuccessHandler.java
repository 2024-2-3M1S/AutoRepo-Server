package org.autorepo.server.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.token.dto.TokenResponse;
import org.autorepo.server.domain.token.service.TokenService;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.common.auth.jwt.JwtTokenProvider;
import org.autorepo.server.global.error.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.autorepo.server.global.error.ErrorCode.USER_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String githubEmail = oAuth2User.getAttribute("email");
        if (githubEmail == null) {
            githubEmail = oAuth2User.getAttribute("login");
        }

        User user = userRepository.findByGithubId(githubEmail)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        String jwtAccessToken = jwtTokenProvider.issueAccessToken(user.getUserId());
        String jwtRefreshToken = jwtTokenProvider.issueRefreshToken(user.getUserId());

        // RefreshToken 저장
        tokenService.saveRefreshToken(user.getUserId(), jwtRefreshToken);

        // 클라이언트로 리다이렉트
        String redirectUrl = "http://localhost:3000/oauth2/success?accessToken=" + jwtAccessToken + "&refreshToken=" + jwtRefreshToken;
        response.sendRedirect(redirectUrl);
    }
}
