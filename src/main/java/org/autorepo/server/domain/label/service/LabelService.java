package org.autorepo.server.domain.label.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.label.dto.request.LabelListRequestDto;
import org.autorepo.server.domain.label.dto.request.UploadLabalRequestDto;
import org.autorepo.server.domain.label.entity.Label;
import org.autorepo.server.domain.label.entity.LabelGenerateType;
import org.autorepo.server.domain.label.repository.LabelRepository;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.domain.repo.service.GitHubService;
import org.autorepo.server.domain.user.entity.User;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.error.ErrorCode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class LabelService {
    private static final String GITHUB_LABEL_API = "https://api.github.com/repos/%s/%s/labels";

    private final GitHubService gitHubService;
    private final UserRepository userRepository;

    public void uploadLabel(UploadLabalRequestDto uploadLabelRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));

        HttpHeaders headers = new HttpHeaders();
        String url = gitHubService.createGitHubApiUrl(GITHUB_LABEL_API, uploadLabelRequestDto.repoUrl(), null, headers, user.getGithubToken());

        try {
            deleteAllLabels(url, headers);
            for (LabelListRequestDto label : uploadLabelRequestDto.labels()) {
                createLabel(url, headers, label);
            }
        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.GITHUB_LABEL_CREATE_ERROR.getMessage(), e);
        }
    }

    private void deleteAllLabels(String url, HttpHeaders headers) {
        try {
            String responseBody = gitHubService.sendRequest(url, HttpMethod.GET, headers, null).getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) return;

            JSONArray existingLabels = new JSONArray(responseBody);
            for (int i = 0; i < existingLabels.length(); i++) {
                JSONObject label = existingLabels.getJSONObject(i);
                String labelName = label.getString("name");
                String deleteUrl = url + "/" + labelName;
                gitHubService.sendRequest(deleteUrl, HttpMethod.DELETE, headers, null);
            }
        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.GITHUB_LABEL_DELETE_ERROR.getMessage(), e);
        }
    }

    private void createLabel(String url, HttpHeaders headers, LabelListRequestDto label) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", label.labelName());
        jsonBody.put("color", label.color());
        jsonBody.put("description", label.description());

        try {
            gitHubService.sendRequest(url, HttpMethod.POST, headers, jsonBody.toString());
        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.GITHUB_LABEL_CREATE_ERROR.getMessage(), e);
        }
    }

/* DB 저장 로직
    private void deleteAllDBLabels(LabelGenerateType labelGenerateType, Repo repo) {
        try {
            labelRepository.deleteByLabelGenerateTypeAndRepo(labelGenerateType, repo);
        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.LABEL_DELETE_ERROR.getMessage(), e);
        }
    }

    private void createAndSaveLabel(String url, HttpHeaders headers, LabelListRequestDto labelDto, Repo repo, LabelGenerateType labelGenerateType) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", labelDto.labelName());
        jsonBody.put("color", labelDto.color());
        jsonBody.put("description", labelDto.description());

        try {
            gitHubService.sendRequest(url, HttpMethod.POST, headers, jsonBody.toString());

            Label newLabel = Label.builder()
                    .name(labelDto.labelName())
                    .color(labelDto.color())
                    .labelDescription(labelDto.description())
                    .labelGenerateType(labelGenerateType)
                    .repo(repo)
                    .build();
            labelRepository.save(newLabel);

        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.GITHUB_LABEL_CREATE_ERROR.getMessage(), e);
        }
    }

 */
}
