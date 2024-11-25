package org.autorepo.server.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.common.SuccessResponse;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final UserRepository userRepository;
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(()
                -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(SuccessResponse.ok(user));
    }
}
