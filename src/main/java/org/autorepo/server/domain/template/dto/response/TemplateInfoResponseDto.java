package org.autorepo.server.domain.template.dto.response;

public record TemplateInfoResponseDto(
        Long templateId,
        String type,
        String title,
        String content
) {
}
