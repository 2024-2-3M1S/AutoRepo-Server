package org.autorepo.server.domain.readme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autorepo.server.domain.readme.dto.request.UploadReadmeRequest;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.EntityNotFoundException;
import org.autorepo.server.global.error.exception.InternalServerException;
import org.autorepo.server.global.utils.GitHubService;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.autorepo.server.global.error.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UploadReadmeService {

    private final GitHubService gitHubService;
    private final UserRepository userRepository;

    private static final String GITHUB_README_API = "https://api.github.com/repos/%s/%s/contents/README.md";

    public void uploadReadme(UploadReadmeRequest uploadReadmeRequest, Long userId) {
        // User ID로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        String githubToken = user.getGithubToken();
        if (githubToken == null || githubToken.isEmpty()) {
            throw new EntityNotFoundException(ErrorCode.GITHUB_TOKEN_NOT_FOUND);
        }

        String repoUrl = uploadReadmeRequest.repoUrl();
        String[] repoInfo = repoUrl.replace("https://github.com/", "").split("/");
        if (repoInfo.length < 2) {
            throw new IllegalArgumentException("Invalid repository URL: " + repoUrl);
        }
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        String content = uploadReadmeRequest.content();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);  // GitHub Token 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = String.format(GITHUB_README_API, owner, repo);

        String base64Content = Base64.getEncoder().encodeToString(content.getBytes());
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("message", "Update README.md");
        jsonBody.put("content", base64Content);

        try {
            // 기존 파일의 SHA 값 가져오기
            String sha = gitHubService.getFileSha(url, headers);
            if (sha != null) {
                jsonBody.put("sha", sha);
            }
        } catch (Exception e) {
            throw new EntityNotFoundException(README_NOT_FOUND);
        }

        try {
            gitHubService.sendRequest(url, HttpMethod.PUT, headers, jsonBody.toString());
        } catch (Exception e) {
            throw new InternalServerException(GITHUB_README_UPLOAD_ERROR);
        }
    }
}
