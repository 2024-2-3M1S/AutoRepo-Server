package org.autorepo.server.global.utils;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.InternalServerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.autorepo.server.global.error.ErrorCode.*;

@RequiredArgsConstructor
@Service
public class GitHubService {
    private final RestTemplate restTemplate = new RestTemplate();

    // 저장소 URL에서 owner와 repo 정보를 파싱
    public String[] parseRepositoryUrl(String repoUrl) {
        String[] parts = repoUrl.split("/");
        if (parts.length < 5) {
            throw new InternalServerException(REPO_PARSE_ERROR);

        }
        String owner = parts[3];
        String repo = parts[4].replace(".git", "");
        return new String[]{owner, repo};
    }

    // GitHub API URL 생성
    public String createGitHubApiUrl(String apiTemplate, String repoUrl, String path, HttpHeaders headers, String token) {
        String[] repoInfo = parseRepositoryUrl(repoUrl);
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");
        return String.format(apiTemplate, repoInfo[0], repoInfo[1], path != null ? path : "");
    }

    // GitHub API 요청
    public ResponseEntity<String> sendRequest(String url, HttpMethod method, HttpHeaders headers, String body) {
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            return restTemplate.exchange(url, method, request, String.class);
        } catch (HttpClientErrorException e) {
            throw new InternalServerException(GITHUB_API_ERROR);
        }
    }

    // GitHub API로 SHA 조회(기존 값 존재 유무 확인)
    public String getFileSha(String url, HttpHeaders headers) {
        try {
            ResponseEntity<String> response = sendRequest(url, HttpMethod.GET, headers, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                return new org.json.JSONObject(response.getBody()).getString("sha");
            }
        } catch (Exception e) {
            throw new InternalServerException(UNPROCESSABLE_ENTITY);
        }
        return null;
    }
}
