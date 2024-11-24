package org.autorepo.server.domain.label.dto.request;

import java.util.List;

public record UploadLabalRequestDto(
        Long userId,
        String repoUrl,
        List<LabelListRequestDto> labels
) {
}
