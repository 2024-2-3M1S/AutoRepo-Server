package org.autorepo.server.domain.template.dto.request;

import org.autorepo.server.domain.template.entity.TemplateType;

public record CreateTemplateRequestDto(
        Long userId,
        String repoUrl,
        TemplateType type,
        String content
) {
}
