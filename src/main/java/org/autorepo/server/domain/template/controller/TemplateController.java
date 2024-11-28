package org.autorepo.server.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.template.dto.request.ShareTemplateRequestDto;
import org.autorepo.server.domain.template.dto.request.UploadTemplateRequestDto;
import org.autorepo.server.domain.template.dto.response.DashboardTemplateResponseDto;
import org.autorepo.server.domain.template.dto.response.TemplateInfoResponseDto;
import org.autorepo.server.domain.template.dto.response.TemplateListResponseDto;
import org.autorepo.server.domain.template.entity.TemplateType;
import org.autorepo.server.domain.template.service.TemplateService;
import org.autorepo.server.global.common.SuccessResponse;
import org.autorepo.server.global.utils.MarkdownToImageConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/template")
@RestController
public class TemplateController {

    private final TemplateService templateService;
    private final MarkdownToImageConverter markdownToImageConverter;

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse<?>> uploadTemplate(@RequestBody UploadTemplateRequestDto uploadTemplateRequestDto, @AuthenticationPrincipal Long userId) {
        templateService.uploadTemplate(uploadTemplateRequestDto,userId);
        return SuccessResponse.created(null);
    }


    @PostMapping("/share")
    public ResponseEntity<SuccessResponse<?>> shareTemplate(@RequestBody ShareTemplateRequestDto shareTemplateRequestDto) {
        // 마크다운 이미지 업로드
        String imageUrl = markdownToImageConverter.convertMarkdownToImage(shareTemplateRequestDto.content(), shareTemplateRequestDto.title());
        templateService.saveTemplate(shareTemplateRequestDto, imageUrl);
        return SuccessResponse.created(null);
    }

    @GetMapping("/{type}")
    public ResponseEntity<SuccessResponse<?>> getAllTemplate(@PathVariable TemplateType type) {
        List<TemplateListResponseDto> templateListResponseDto = templateService.getAllTemplate(type);
        return SuccessResponse.ok(templateListResponseDto);
    }

    @GetMapping("/dash-board")
    public ResponseEntity<SuccessResponse<?>> getDashBoard(@AuthenticationPrincipal Long userId) {
        DashboardTemplateResponseDto dashboardTemplateResponseDto = templateService.getDashBoardTemplates(userId);
        return SuccessResponse.ok(dashboardTemplateResponseDto);
    }

    @GetMapping("/{type}/{templateId}")
    public ResponseEntity<SuccessResponse<?>> getTemplateInfo(@PathVariable Long templateId, @PathVariable String type) {
        TemplateInfoResponseDto templateInfoResponseDto = templateService.getTemplateInfo(templateId,type);
        return SuccessResponse.ok(templateInfoResponseDto);
    }


}
