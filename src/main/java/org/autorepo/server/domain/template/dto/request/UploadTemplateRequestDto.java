package org.autorepo.server.domain.template.dto.request;

import org.autorepo.server.domain.template.entity.TemplateType;

public record UploadTemplateRequestDto(
        Long userId,
        String repoUrl,
        TemplateType type,
        String content
) {
}
