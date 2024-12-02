package org.autorepo.server.domain.repo.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.dto.response.RepoResponse;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.BusinessException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.autorepo.server.domain.repo.service.RepoService;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/repo")
public class RepoController {

    private final RepoService repoService;

    @GetMapping("/fetch")
    public List<RepoResponse> fetchUserRepos(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_MISSING);
        }
        return repoService.fetchUserReposByUserId(userId);
    }
}