package org.autorepo.server.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.template.dto.request.CreateTemplateRequestDto;
import org.autorepo.server.domain.template.service.TemplateService;
import org.autorepo.server.global.common.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/template")
@RestController
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse<?>> uploadTemplate(@RequestBody CreateTemplateRequestDto createTemplateRequestDto) {
        templateService.uploadTemplate(createTemplateRequestDto);
        return SuccessResponse.created(null);
    }
}
