package org.autorepo.server.domain.readme.controller;

import com.sun.net.httpserver.Authenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autorepo.server.domain.readme.dto.request.ReadmeRequest;
import org.autorepo.server.domain.readme.dto.request.UploadReadmeRequest;
import org.autorepo.server.domain.readme.dto.response.ReadmeResponse;
import org.autorepo.server.domain.readme.service.CreateReadmeService;
import org.autorepo.server.domain.readme.service.UploadReadmeService;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.autorepo.server.global.common.SuccessResponse;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.EntityNotFoundException;
import org.autorepo.server.global.utils.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.autorepo.server.global.error.ErrorCode.USER_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/readme")
@RestController
public class ReadmeController {

    private final CreateReadmeService createReadmeService;
    private final UploadReadmeService uploadReadmeService;

    @PostMapping("/generate")
    public ResponseEntity<ReadmeResponse> generateMarkdown(@RequestBody ReadmeRequest readmeRequest) {
        log.info("Received request: {}", readmeRequest);
        try {
            String markdown = createReadmeService.generateMarkdown(readmeRequest);
            ReadmeResponse response = new ReadmeResponse(markdown);
            log.info("Generated markdown: {}", markdown);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while generating markdown", e);
            throw new RuntimeException(ErrorCode.README_GENERATE_ERROR.getMessage(), e);
        }
    }

    @PutMapping("/upload")
    public ResponseEntity<SuccessResponse<?>> uploadReadme(
            @RequestBody UploadReadmeRequest uploadReadmeRequest,
            @AuthenticationPrincipal String githubLogin) {
        if (githubLogin == null) {
            throw new IllegalArgumentException("User login is null.");
        }
        uploadReadmeService.uploadReadme(uploadReadmeRequest, githubLogin);
        return SuccessResponse.created(null);
    }

}