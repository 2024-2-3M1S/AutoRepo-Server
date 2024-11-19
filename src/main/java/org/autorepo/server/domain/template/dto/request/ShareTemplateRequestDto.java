package org.autorepo.server.domain.template.dto.request;

import org.autorepo.server.domain.template.entity.TemplateType;

public record ShareTemplateRequestDto(
        Long userId,
        TemplateType type,
        String title,
        String content
) {
}
