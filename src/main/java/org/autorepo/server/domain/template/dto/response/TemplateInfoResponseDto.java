package org.autorepo.server.domain.template.dto.response;

public record TemplateInfoResponseDto(
        Long id,
        String type,
        String title,
        String content
) {
}
