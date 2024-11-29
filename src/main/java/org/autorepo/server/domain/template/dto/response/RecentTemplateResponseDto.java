package org.autorepo.server.domain.template.dto.response;

import java.time.LocalDateTime;

public record RecentTemplateResponseDto(
        Long id,
        String type,
        String title,
        String imageUrl,
        LocalDateTime modifiedAt


) {

}
