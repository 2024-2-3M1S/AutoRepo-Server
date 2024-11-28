package org.autorepo.server.domain.template.dto.response;

public record RandomTemplateResponseDto(
        Long id,
        String type,
        String title,
        String imageUrl
) {
}
