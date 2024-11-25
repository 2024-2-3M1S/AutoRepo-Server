package org.autorepo.server.domain.template.dto.response;

public record RandomTemplateResponseDto(
        String type,
        String title,
        String content
) {
}
