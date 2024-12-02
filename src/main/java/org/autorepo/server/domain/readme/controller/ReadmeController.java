package org.autorepo.server.domain.readme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autorepo.server.domain.readme.dto.request.ReadmeRequest;
import org.autorepo.server.domain.readme.dto.request.UploadReadmeRequest;
import org.autorepo.server.domain.readme.dto.response.ReadmeResponse;
import org.autorepo.server.domain.readme.service.CreateReadmeService;
import org.autorepo.server.domain.readme.service.UploadReadmeService;
import org.autorepo.server.global.common.SuccessResponse;
import org.autorepo.server.global.error.ErrorCode;
import org.autorepo.server.global.error.exception.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/readme")
@RestController
public class ReadmeController {

    private final CreateReadmeService createReadmeService;
    private final UploadReadmeService uploadReadmeService;

    @PostMapping("/generate")
    public ResponseEntity<ReadmeResponse> generateAndSaveReadme(@RequestBody ReadmeRequest readmeRequest) {
        log.info("Received request to generate and save README: {}", readmeRequest);
        try {
            ReadmeResponse response = createReadmeService.generateAndSaveReadme(readmeRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while generating markdown", e);
            throw new RuntimeException(ErrorCode.README_GENERATE_ERROR.getMessage(), e);
        }
    }

    @PutMapping("/upload")
    public ResponseEntity<SuccessResponse<?>> uploadReadme(@RequestBody UploadReadmeRequest uploadReadmeRequest, @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new EntityNotFoundException(ErrorCode.USER_ID_MISSING);
        }
        uploadReadmeService.uploadReadme(uploadReadmeRequest, userId);
        return SuccessResponse.created(null);
    }
}