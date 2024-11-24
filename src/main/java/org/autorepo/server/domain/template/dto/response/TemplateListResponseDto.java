package org.autorepo.server.domain.template.dto.response;

import org.autorepo.server.domain.template.entity.Template;

public record TemplateListResponseDto(
        Long templateId,
        String title,
        String content
) {
    public static TemplateListResponseDto of(Template template) {
        return new TemplateListResponseDto(
                template.getTemplateId(),
                template.getTitle(),
                template.getContent()
        );
    }
}
