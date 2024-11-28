package org.autorepo.server.domain.template.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.readme.entity.Readme;
import org.autorepo.server.domain.readme.repository.ReadmeRepository;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.global.utils.GitHubService;
import org.autorepo.server.domain.template.dto.request.ShareTemplateRequestDto;
import org.autorepo.server.domain.template.dto.request.UploadTemplateRequestDto;
import org.autorepo.server.domain.template.dto.response.DashboardTemplateResponseDto;
import org.autorepo.server.domain.template.dto.response.RandomTemplateResponseDto;
import org.autorepo.server.domain.template.dto.response.RecentTemplateResponseDto;
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

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TemplateService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/contents/%s";

    private final GitHubService gitHubService;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final RepoRepository repoRepository;
    private final ReadmeRepository readmeRepository;

    // 템플릿 업로드
    public void uploadTemplate(UploadTemplateRequestDto uploadTemplateRequestDto, Long userId) {
        User user = userRepository.findById(userId)
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
    public void saveTemplate(ShareTemplateRequestDto shareTemplateRequestDto, String imageUrl) {

        Repo repo = repoRepository.findByRepoUrl(shareTemplateRequestDto.repoUrl())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.REPO_NOT_FOUND.getMessage()));

        Template existingTemplate = templateRepository.findByRepoAndType(repo, shareTemplateRequestDto.type())
                .orElse(null);
        if (existingTemplate != null) {
            existingTemplate.updateContent(shareTemplateRequestDto.title(), shareTemplateRequestDto.content(), imageUrl);
            templateRepository.save(existingTemplate);
        } else {
            // 새로운 템플릿 생성
            Template newTemplate = Template.builder()
                    .repo(repo)
                    .title(shareTemplateRequestDto.title())
                    .content(shareTemplateRequestDto.content())
                    .type(shareTemplateRequestDto.type())
                    .imageUrl(imageUrl)
                    .build();

            templateRepository.save(newTemplate);
        }
    }


    // 템플릿 조회
    public List<TemplateListResponseDto> getAllTemplate(TemplateType type) {
        return templateRepository.findAllByType(type).stream()
                .map(TemplateListResponseDto::of)
                .collect(Collectors.toList());
    }

    // 대시보드 템플릿 조회
    public DashboardTemplateResponseDto getDashBoardTemplates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));

        // 전체 템플릿 리드미
        List<Template> allTemplates = templateRepository.findAll();
        List<Readme> allReadmes = readmeRepository.findAll();

        // 내 템플릿, 리드미
        List<Repo> userRepos = repoRepository.findAllByUser(user);
        List<Template> myTemplates = templateRepository.findAllByRepoIn(userRepos);
        List<Readme> myReadmes = readmeRepository.findAllByRepoIn(userRepos);

        // 랜덤 템플릿
        List<RandomTemplateResponseDto> randomTemplates = combineTemplatesAndReadmes(allTemplates, allReadmes).stream()
                .sorted((o1, o2) -> new Random().nextInt(3) - 1)
                .map(template -> new RandomTemplateResponseDto(
                        template.templateId(),
                        template.type(),
                        template.title(),
                        template.imageUrl()

                ))
                .toList();

        // 최근 템플릿
        List<RecentTemplateResponseDto> recentTemplates = combineTemplatesAndReadmes(myTemplates, myReadmes).stream()
                .sorted(Comparator.comparing(RecentTemplateResponseDto::modifiedAt).reversed())
                .map(template -> new RecentTemplateResponseDto(
                        template.templateId(),
                        template.type(),
                        template.title(),
                        template.imageUrl(),
                        template.modifiedAt()
                ))
                .toList();

        return new DashboardTemplateResponseDto(randomTemplates, recentTemplates);
    }

    //템플릿, 리드미 합치기
    private List<RecentTemplateResponseDto> combineTemplatesAndReadmes(List<Template> templates, List<Readme> readmes) {
        List<RecentTemplateResponseDto> result = new ArrayList<>();

        templates.forEach(template -> result.add(new RecentTemplateResponseDto(
                template.getTemplateId(),
                template.getType().name(),
                template.getTitle(),
                template.getImageUrl(),
                template.getModifiedAt()
        )));

        readmes.forEach(readme -> result.add(new RecentTemplateResponseDto(
                readme.getReadmeId(),
                "README",
                readme.getTitle(),
                readme.getImageUrl(),
                readme.getModifiedAt()
        )));

        return result;
    }

}
