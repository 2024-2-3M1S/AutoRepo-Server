package org.autorepo.server.domain.template.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.domain.template.dto.request.ShareTemplateRequestDto;
import org.autorepo.server.domain.template.dto.request.TemplateListResponseDto;
import org.autorepo.server.domain.template.dto.request.UploadTemplateRequestDto;
import org.autorepo.server.domain.template.entity.Template;
import org.autorepo.server.domain.template.entity.TemplateType;
import org.autorepo.server.domain.template.repository.TemplateRepository;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TemplateService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/contents/%s";
    private static final String PR_TEMPLATE_PATH = ".github/pull_request_template.md";
    private static final String ISSUE_TEMPLATE_PATH = ".github/ISSUE_TEMPLATE/issue_template.md";

    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final RepoRepository repoRepository;

    // PR/ISSUE 템플릿 업로드
    public void uploadTemplate(UploadTemplateRequestDto uploadTemplateRequestDto) {

        User user = userRepository.findById(uploadTemplateRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 사용자를 찾을 수 없습니다: " + uploadTemplateRequestDto.userId()));

        boolean isPR = uploadTemplateRequestDto.type() == TemplateType.PR;

        //이슈 메타 데이터 정보 임시 고정
        String metaContent = "---\nname: Issue template\nabout: Issue template\ntitle: ''\nlabels: ''\nassignees: ''\n---";
        String content = isPR ? uploadTemplateRequestDto.content() : metaContent + "\n" + uploadTemplateRequestDto.content();
        String path = isPR ? PR_TEMPLATE_PATH : ISSUE_TEMPLATE_PATH;

        saveFileToGitHub(uploadTemplateRequestDto.repoUrl(), path, content, uploadTemplateRequestDto.type(), user.getGithubToken());
    }


    // GitHub API 요청
    private void saveFileToGitHub(String repoUrl, String path, String content, TemplateType type, String token) {
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
    private String[] parseRepositoryUrl(String repoUrl) {
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

    // 템플릿 저장
    public void saveTemplate(ShareTemplateRequestDto shareTemplateRequestDto) {

        boolean templateExists = templateRepository.findByTitleAndContent(shareTemplateRequestDto.title(), shareTemplateRequestDto.content())
                .isPresent();

        if (!templateExists) {
            Template template = Template.builder()
                    .title(shareTemplateRequestDto.title())
                    .content(shareTemplateRequestDto.content())
                    .type(shareTemplateRequestDto.type())
                    .build();

            templateRepository.save(template);
        }
    }

    // 템플릿 리스트 조회
    public List<TemplateListResponseDto> getAllTemplate(TemplateType type) {
        List<Template> templates = templateRepository.findAllByType(type);

        return templates.stream()
                .map(TemplateListResponseDto::of)
                .collect(Collectors.toList());
    }


}
