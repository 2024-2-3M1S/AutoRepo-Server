package org.autorepo.server.domain.template.dto.response;

import java.time.LocalDateTime;

public record RecentTemplateResponseDto(
        LocalDateTime modifiedAt,
        String type,
        String title,
        String content
) {

}
