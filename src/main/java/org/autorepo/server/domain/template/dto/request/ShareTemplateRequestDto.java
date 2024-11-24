package org.autorepo.server.domain.template.dto.request;

import org.autorepo.server.domain.template.entity.TemplateType;

public record ShareTemplateRequestDto(
        Long userId,
        String repoUrl,
        TemplateType type,
        String title,
        String content
) {
}
