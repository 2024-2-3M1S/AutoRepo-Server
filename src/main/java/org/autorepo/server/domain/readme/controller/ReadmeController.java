package org.autorepo.server.domain.readme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.autorepo.server.domain.readme.dto.request.ReadmeRequest;
import org.autorepo.server.domain.readme.dto.response.ReadmeResponse;
import org.autorepo.server.domain.readme.service.ReadmeService;
import org.autorepo.server.global.error.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/readme")
@RestController
public class ReadmeController {

    private final ReadmeService readmeService;

    @PostMapping("/generate")
    public ResponseEntity<ReadmeResponse> generateMarkdown(@RequestBody ReadmeRequest readmeRequest) {
        log.info("Received request: {}", readmeRequest);
        try {
            String markdown = readmeService.generateMarkdown(readmeRequest);
            ReadmeResponse response = new ReadmeResponse(markdown);
            log.info("Generated markdown: {}", markdown);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while generating markdown", e);
            throw new RuntimeException(ErrorCode.README_GENERATE_ERROR.getMessage(), e);
        }
    }
}