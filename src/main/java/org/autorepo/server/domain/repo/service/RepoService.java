package org.autorepo.server.domain.repo.service;

import lombok.RequiredArgsConstructor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional
public class RepoService {

    private final RepoRepository repoRepository;
    private final UserRepository userRepository;

    public void saveSingleRepo(Repo repo, User user) {
        if (!userRepository.existsById(user.getUserId())) {
            userRepository.save(user);
        }
        repo.setUser(user);
        repoRepository.save(repo);
    }

    @Transactional
    public List<Repo> fetchAndSaveRepos(User user, String githubToken) {
        // User가 영속화되지 않았으면 저장 후 영속화된 객체로 업데이트
        if (user.getUserId() == null || !userRepository.existsById(user.getUserId())) {
            user = userRepository.save(user);
        }

        // GitHub API 호출
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/user/repos";

        var headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        List<Map<String, Object>> repos = response.getBody();
        List<Repo> savedRepos = new ArrayList<>();

        // 각 레포지토리를 Repo 엔티티로 변환 후 저장
        if (repos != null) {
            for (Map<String, Object> repoData : repos) {
                String repoName = (String) repoData.get("name");
                String repoUrl = (String) repoData.get("html_url");

                // 중복 확인
                if (!repoRepository.findByRepoUrl(repoUrl).isPresent()) {
                    Repo repo = new Repo();
                    repo.setRepoName(repoName);
                    repo.setRepoUrl(repoUrl);
                    repo.setUser(user); // User 설정

                    savedRepos.add(repoRepository.save(repo)); // Repo 저장
                }
            }
        }

        return savedRepos;
    }
}
