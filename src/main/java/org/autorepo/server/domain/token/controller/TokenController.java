package org.autorepo.server.domain.token.controller;

import lombok.extern.slf4j.Slf4j;
import org.autorepo.server.domain.token.dto.TokenResponse;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.token.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@RequestHeader("RefreshToken") String refreshToken) {
        Long userId = tokenService.findUserIdByRefreshToken(refreshToken);
        String newAccessToken = tokenService.generateAccessToken(userId);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken, refreshToken));
    }
}