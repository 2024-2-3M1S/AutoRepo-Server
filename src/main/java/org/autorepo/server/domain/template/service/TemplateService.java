package org.autorepo.server.domain.template.service;


import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.readme.entity.Readme;
import org.autorepo.server.domain.readme.repository.ReadmeRepository;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.domain.template.dto.request.ShareTemplateRequestDto;
import org.autorepo.server.domain.template.dto.request.UploadTemplateRequestDto;
import org.autorepo.server.domain.template.dto.response.*;
import org.autorepo.server.domain.template.entity.Template;
import org.autorepo.server.domain.template.entity.TemplateType;
import org.autorepo.server.domain.template.repository.TemplateRepository;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.error.exception.EntityNotFoundException;
import org.autorepo.server.global.error.exception.InternalServerException;
import org.autorepo.server.global.utils.GitHubService;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

import static org.autorepo.server.global.error.ErrorCode.*;

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
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

    // 1. 템플릿 내용과 파일 경로 생성
    Map<String, String> templateData = generateTemplateData(uploadTemplateRequestDto);

    // 2. GitHub API URL 생성
    String url = gitHubService.createGitHubApiUrl(
            GITHUB_API_URL, uploadTemplateRequestDto.repoUrl(), templateData.get("path"), new HttpHeaders(), user.getGithubToken()
    );

    // 3. GitHub로 템플릿 업로드 요청
    sendTemplateToGitHub(url, templateData.get("content"), uploadTemplateRequestDto.type(), user.getGithubToken());
}

    private Map<String, String> generateTemplateData(UploadTemplateRequestDto uploadTemplateRequestDto) {
        boolean isPR = uploadTemplateRequestDto.type() == TemplateType.PR;
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        String metaContent = String.format("---\nname: Issue template-%s\nabout: Issue template 입니다.\ntitle: ''\nlabels: ''\nassignees: ''\n---", timestamp);
        String formattedContent = uploadTemplateRequestDto.content().replace("\\n", "  \n");
        String content = isPR ? formattedContent : metaContent + "\n" + formattedContent;

        String path = isPR
                ? String.format(".github/pull_request_template.md")
                : String.format(".github/ISSUE_TEMPLATE/--option-----filename-%s.md", timestamp);

        Map<String, String> templateData = new HashMap<>();
        templateData.put("content", content);
        templateData.put("path", path);

        return templateData;
    }

    // GitHub로 템플릿 업로드 요청
    private void sendTemplateToGitHub(String url, String content, TemplateType type, String githubToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        String base64Content = java.util.Base64.getEncoder().encodeToString(content.getBytes());

        JSONObject jsonBody = new JSONObject();
        if (type == TemplateType.ISSUE) {
            jsonBody.put("message", "Add new Issue Template");
        } else if (type == TemplateType.PR) {
            jsonBody.put("message", " Update new PR Template");
        }
        jsonBody.put("content", base64Content);

        try {
            if (type == TemplateType.ISSUE) {
                // 이슈 템플릿 업로드
                gitHubService.sendRequest(url, HttpMethod.PUT, headers, jsonBody.toString());
            } else if (type == TemplateType.PR) {

                // PR 템플릿 업로드
                String sha = null;
                try {
                    sha = gitHubService.getFileSha(url, headers);
                } catch (HttpClientErrorException.NotFound e) {
                }
                // 기존 템플릿 SHA가 있는 경우 JSON에 추가
                if (sha != null) {
                    jsonBody.put("sha", sha);
                }
                gitHubService.sendRequest(url, HttpMethod.PUT, headers, jsonBody.toString());
            } else {
                throw new IllegalArgumentException("Unsupported Template Type: " + type);
            }
        } catch (Exception e) {
            System.out.println("GitHub API 호출 실패: " + e.getMessage());
            throw new InternalServerException(GITHUB_TEMPLATE_UPLOAD_ERROR);
        }
    }



    // 템플릿 저장
    public void saveTemplate(ShareTemplateRequestDto shareTemplateRequestDto, String imageUrl) {
        Repo repo = repoRepository.findByRepoUrl(shareTemplateRequestDto.repoUrl())
                .orElseThrow(() -> new EntityNotFoundException(REPO_NOT_FOUND));

        Template newTemplate = Template.builder()
                .repo(repo)
                .title(shareTemplateRequestDto.title())
                .content(shareTemplateRequestDto.content())
                .type(shareTemplateRequestDto.type())
                .imageUrl(imageUrl)
                .build();

        templateRepository.save(newTemplate);
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
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

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
                        template.id(),
                        template.type(),
                        template.title(),
                        template.imageUrl()

                ))
                .toList();

        // 최근 템플릿
        List<RecentTemplateResponseDto> recentTemplates = combineTemplatesAndReadmes(myTemplates, myReadmes).stream()
                .sorted(Comparator.comparing(RecentTemplateResponseDto::modifiedAt).reversed())
                .map(template -> new RecentTemplateResponseDto(
                        template.id(),
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

    public TemplateInfoResponseDto getTemplateInfo(Long id, String type) {
        if (type.equals("README")) {
            Readme readme = readmeRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(README_NOT_FOUND));
            return new TemplateInfoResponseDto(readme.getReadmeId(), "README", readme.getTitle(), readme.getContent());
        } else {
            Template template = templateRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
            return new TemplateInfoResponseDto(template.getTemplateId(), template.getType().name(), template.getTitle(), template.getContent());
        }


    }
}
