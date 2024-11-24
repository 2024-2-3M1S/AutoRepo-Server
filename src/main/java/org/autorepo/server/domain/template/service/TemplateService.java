package org.autorepo.server.domain.template.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.service.GitHubService;
import org.autorepo.server.domain.template.dto.request.ShareTemplateRequestDto;
import org.autorepo.server.domain.template.dto.request.UploadTemplateRequestDto;
import org.autorepo.server.domain.template.dto.response.TemplateListResponseDto;
import org.autorepo.server.domain.template.entity.Template;
import org.autorepo.server.domain.template.entity.TemplateType;
import org.autorepo.server.domain.template.repository.TemplateRepository;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.error.ErrorCode;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TemplateService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/contents/%s";

    private final GitHubService gitHubService;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;

    // 템플릿 업로드
    public void uploadTemplate(UploadTemplateRequestDto uploadTemplateRequestDto) {
        User user = userRepository.findById(uploadTemplateRequestDto.userId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));

        // PR/ISSUE 템플릿 구분
        boolean isPR = uploadTemplateRequestDto.type() == TemplateType.PR;
        String metaContent = "---\nname: Issue template\nabout: Issue template\ntitle: ''\nlabels: ''\nassignees: ''\n---";
        String content = isPR ? uploadTemplateRequestDto.content() : metaContent + "\n" + uploadTemplateRequestDto.content();
        String path = isPR ? ".github/pull_request_template.md" : ".github/ISSUE_TEMPLATE/issue_template.md";

        HttpHeaders headers = new HttpHeaders();
        String url = gitHubService.createGitHubApiUrl(GITHUB_API_URL, uploadTemplateRequestDto.repoUrl(), path, headers, user.getGithubToken());
        String base64Content = java.util.Base64.getEncoder().encodeToString(content.getBytes());
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("message", "Update " + uploadTemplateRequestDto.type() + " Template");
        jsonBody.put("content", base64Content);

        //기존 템플릿 존재 여부 확인
        try {
            String sha = gitHubService.getFileSha(url, headers);
            if (sha != null) {
                jsonBody.put("sha", sha);
            }
            gitHubService.sendRequest(url, HttpMethod.PUT, headers, jsonBody.toString());
        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.GITHUB_TEMPLATE_UPLOAD_ERROR.getMessage(), e);
        }
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

    // 템플릿 조회
    public List<TemplateListResponseDto> getAllTemplate(TemplateType type) {
        return templateRepository.findAllByType(type).stream()
                .map(TemplateListResponseDto::of)
                .collect(Collectors.toList());
    }
}
