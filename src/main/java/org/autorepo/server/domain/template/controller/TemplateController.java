package org.autorepo.server.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.template.dto.request.CreateTemplateRequestDto;
import org.autorepo.server.domain.template.dto.request.TemplateListResponseDto;
import org.autorepo.server.domain.template.entity.TemplateType;
import org.autorepo.server.domain.template.service.TemplateService;
import org.autorepo.server.global.common.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/template")
@RestController
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse<?>> uploadTemplate(@RequestBody CreateTemplateRequestDto createTemplateRequestDto) {
        templateService.uploadTemplate(createTemplateRequestDto);
        templateService.saveTemplate(createTemplateRequestDto);
        return SuccessResponse.created(null);
    }

    @GetMapping("/{type}")
    public ResponseEntity<SuccessResponse<?>> getAllTemplate(@PathVariable TemplateType type) {
        List<TemplateListResponseDto> templateListResponseDto = templateService.getAllTemplate(type);
        return SuccessResponse.ok(templateListResponseDto);
    }
}
