package org.autorepo.server.domain.template.dto.response;

public record RandomTemplateResponseDto(
        Long templateId,
        String type,
        String title,
        String imageUrl
) {
}
