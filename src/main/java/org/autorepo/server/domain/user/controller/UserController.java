package org.autorepo.server.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.token.service.TokenService;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.common.SuccessResponse;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.autorepo.server.global.common.SuccessCode.LOGOUT;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(()
                -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(SuccessResponse.ok(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long userId) {
        tokenService.logout(userId);
        return ResponseEntity.ok(SuccessResponse.ok(LOGOUT));
    }
}
