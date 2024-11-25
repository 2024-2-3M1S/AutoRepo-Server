package org.autorepo.server.domain.template.dto.response;

import java.util.List;

public record DashboardTemplateResponseDto(
        List<RandomTemplateResponseDto> randomTemplates,
        List<RecentTemplateResponseDto> recentTemplates
) {
}
