package org.autorepo.server.domain.repo.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.dto.response.RepoResponse;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.entity.UserRole;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional
public class RepoService {

    private final RepoRepository repoRepository;
    private final UserRepository userRepository;

    public List<RepoResponse> fetchUserRepos(String githubToken) {
        String token = githubToken.replace("Bearer ", "");
        String githubId = fetchGithubIdFromToken(token);

        // User 초기화
        User user = userRepository.findByGithubId(githubId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGithubId(githubId);
                    newUser.setGithubToken(token);
                    newUser.setUserRole(UserRole.valueOf("USER"));
                    return userRepository.save(newUser);
                });

        List<Repo> repos = fetchAndSaveRepos(user, token);

        return repos.stream()
                .map(repo -> new RepoResponse(repo.getRepoName(), repo.getRepoUrl(), repo.getUser().getUserId()))
                .toList();
    }

    // Token에서 id 추출
    public String fetchGithubIdFromToken(String token) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/user";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("login")) {
            throw new IllegalArgumentException("Invalid GitHub token");
        }

        return (String) responseBody.get("login");
    }

    @Transactional
    public List<Repo> fetchAndSaveRepos(User user, String githubToken) {
        // GitHub API 호출
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/user/repos";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        List<Map<String, Object>> repos = response.getBody();

        if (repos != null) {
            for (Map<String, Object> repoData : repos) {
                String repoName = (String) repoData.get("name");
                String repoUrl = (String) repoData.get("html_url");

                // 레포지토리가 이미 저장되어 있는지 확인
                if (repoRepository.findByRepoUrl(repoUrl).isEmpty()) {
                    Repo repo = new Repo();
                    repo.setRepoName(repoName);
                    repo.setRepoUrl(repoUrl);
                    repo.setUser(user); // 기존 유저와 매핑
                    repoRepository.save(repo);
                }
            }
        }

        // 해당 유저의 모든 레포지토리를 반환
        return repoRepository.findAllByUser(user);
    }
}

