package org.autorepo.server.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.template.dto.request.ShareTemplateRequestDto;
import org.autorepo.server.domain.template.dto.response.TemplateListResponseDto;
import org.autorepo.server.domain.template.dto.request.UploadTemplateRequestDto;
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
    public ResponseEntity<SuccessResponse<?>> uploadTemplate(@RequestBody UploadTemplateRequestDto uploadTemplateRequestDto) {
        templateService.uploadTemplate(uploadTemplateRequestDto);
        return SuccessResponse.created(null);
    }

    @PostMapping("/share")
    public ResponseEntity<SuccessResponse<?>> shareTemplate(@RequestBody ShareTemplateRequestDto shareTemplateRequestDto) {
        templateService.saveTemplate(shareTemplateRequestDto);
        return SuccessResponse.created(null);
    }


    @GetMapping("/{type}")
    public ResponseEntity<SuccessResponse<?>> getAllTemplate(@PathVariable TemplateType type) {
        List<TemplateListResponseDto> templateListResponseDto = templateService.getAllTemplate(type);
        return SuccessResponse.ok(templateListResponseDto);
    }
}
