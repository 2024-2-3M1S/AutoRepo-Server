package org.autorepo.server.domain.template.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.template.dto.request.CreateTemplateRequestDto;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class TemplateService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/contents/%s";
    private static final String PR_TEMPLATE_PATH = ".github/pull_request_template.md";
    private static final String ISSUE_TEMPLATE_PATH = ".github/ISSUE_TEMPLATE/issue_template.md";

    private final UserRepository userRepository;

    // PR/ISSUE 템플릿 업로드
    public void uploadTemplate(CreateTemplateRequestDto createTemplateRequestDto) {

        User user = userRepository.findById(createTemplateRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 사용자를 찾을 수 없습니다: " + createTemplateRequestDto.userId()));

        //이슈 메타 데이터 정보 임시 고정
        String metaContent = "---\nname: Issue template\nabout: Issue template\ntitle: ''\nlabels: ''\nassignees: ''\n---";
        String content = metaContent  +"\n"+ createTemplateRequestDto.content();

        String path = createTemplateRequestDto.type().equalsIgnoreCase("PR") ? PR_TEMPLATE_PATH : ISSUE_TEMPLATE_PATH;

        saveFileToGitHub(createTemplateRequestDto.repoUrl(), path, content, createTemplateRequestDto.type(), user.getGithubToken());
    }


    // GitHub API 요청
    public void saveFileToGitHub(String repoUrl, String path, String content, String type, String token) {
        RestTemplate restTemplate = new RestTemplate();

        String[] repoInfo = parseRepositoryUrl(repoUrl);
        String owner = repoInfo[0];
        String repo = repoInfo[1];
        String url = String.format(GITHUB_API_URL, owner, repo, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        String sha = getFileShaIfExists(restTemplate, url, headers);
        String base64Content = java.util.Base64.getEncoder().encodeToString(content.getBytes());

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("message", "Update " + type + " Template");
        jsonBody.put("content", base64Content);
        if (sha != null) {
            jsonBody.put("sha", sha);
        }

        HttpEntity<String> request = new HttpEntity<>(jsonBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("파일 저장 실패: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // repo URL에서 owner와 repo 정보를 파싱
    public String[] parseRepositoryUrl(String repoUrl) {
        String[] parts = repoUrl.split("/");
        if (parts.length < 5) {
            throw new IllegalArgumentException("잘못된 저장소 URL 형식입니다.");
        }
        String owner = parts[3];
        String repo = parts[4].replace(".git", "");
        return new String[]{owner, repo};
    }

    // GitHub 저장소에서 SHA값 조회(이전 파일이 존재하는지 확인)
    private String getFileShaIfExists(RestTemplate restTemplate, String url, HttpHeaders headers) {
        try {
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return new JSONObject(response.getBody()).getString("sha");
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

}
