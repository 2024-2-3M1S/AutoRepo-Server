package org.autorepo.server.domain.repo.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.dto.response.RepoResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.service.RepoService;
import org.autorepo.server.domain.user.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/repo")
public class RepoController {

    private final RepoService repoService;

    @GetMapping("/fetch")
    public List<RepoResponse> fetchUserRepos(@RequestHeader("Authorization") String githubToken, User user) {
        String token = githubToken.replace("Bearer ", "");
        List<Repo> repos = repoService.fetchAndSaveRepos(user, token);

        return repos.stream()
                .map(repo -> new RepoResponse(repo.getRepoName(), repo.getRepoUrl(), repo.getUser().getUserId()))
                .toList();
    }
}
