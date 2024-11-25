package org.autorepo.server.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.token.dto.TokenResponse;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.common.SuccessResponse;
import org.autorepo.server.global.common.auth.jwt.JwtTokenProvider;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(()
                -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(SuccessResponse.ok(user));
    }

    // 임시 토큰 발급 API 입니다. 추후 로그인 기능이 완성되면 삭제할 예정입니다
    @PostMapping("/token/{userId}")
    public ResponseEntity<SuccessResponse<?>> getToken(@PathVariable Long userId) {
        String accessToken = jwtTokenProvider.issueAccessToken(userId);
        String refreshToken = jwtTokenProvider.issueRefreshToken(userId);
        return SuccessResponse.created(TokenResponse.of(accessToken, refreshToken));
    }

}
