package org.autorepo.server.domain.template.dto.request;

public record CreateTemplateRequestDto(
        Long userId,
        String repoUrl,
        String content,
        String type
) {
}
