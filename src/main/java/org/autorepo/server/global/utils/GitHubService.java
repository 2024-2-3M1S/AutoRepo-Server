package org.autorepo.server.global.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.InternalServerException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

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
            System.out.println("GitHub API 요청 실패: " + e.getMessage());
            System.out.println("응답 코드: " + e.getStatusCode());
            System.out.println("응답 본문: " + e.getResponseBodyAsString());

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
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                System.out.println("File not found. ");
                return null; // 파일이 없는 경우 null 반환
            }
            throw new InternalServerException(UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            throw new InternalServerException(UNPROCESSABLE_ENTITY);
        }

        return null;
    }


//    public String getUsernameFromPAT(String token) {
//        // GitHub API 호출로 사용자 정보를 가져옵니다.
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token);
//        headers.set("Accept", "application/vnd.github.v3+json");
//
//        String githubApiUrl = "https://api.github.com/user";
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<Map> response = restTemplate.exchange(
//                githubApiUrl, HttpMethod.GET, entity, Map.class);
//
//        return (String) response.getBody().get("login"); // login 정보 반환
//    }

    public String parseUsernameFromResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.get("login").asText(); // login 필드 값 추출
        } catch (Exception e) {
            System.err.println("Failed to parse GitHub API response: " + e.getMessage());
            throw new RuntimeException("Error parsing GitHub API response", e);
        }
    }
    public String getUsernameFromPAT(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            System.out.println("Authorization Header: " + headers.get("Authorization"));
            return parseUsernameFromResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("Error in GitHub API call: " + e.getMessage());
            throw e;
        }
    }

}
