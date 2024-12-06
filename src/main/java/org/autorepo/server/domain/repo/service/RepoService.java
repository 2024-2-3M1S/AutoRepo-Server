package org.autorepo.server.domain.repo.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.dto.response.RepoResponse;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.domain.user.entity.User;
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

    public List<RepoResponse> fetchUserReposByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));

        List<Repo> repos = fetchAndSaveRepos(user, user.getGithubToken());

        return repos.stream()
                .map(repo -> new RepoResponse(repo.getRepoName(), repo.getRepoUrl(), user.getUserId()))
                .toList();
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

        // GitHub에서 가져온 URL 리스트
        List<String> githubRepoUrls = repos != null ? repos.stream()
                .map(repoData -> (String) repoData.get("html_url"))
                .toList() : List.of();

        // DB에서 해당 유저의 저장된 레포지토리 리스트 가져오기
        List<Repo> existingRepos = repoRepository.findAllByUser(user);

        // GitHub에 없는 레포지토리 DB에서 삭제
        List<Repo> reposToDelete = existingRepos.stream()
                .filter(repo -> !githubRepoUrls.contains(repo.getRepoUrl()))
                .toList();
        repoRepository.deleteAll(reposToDelete);

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
