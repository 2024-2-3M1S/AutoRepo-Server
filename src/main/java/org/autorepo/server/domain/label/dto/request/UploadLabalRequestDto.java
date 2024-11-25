package org.autorepo.server.domain.label.dto.request;

import org.autorepo.server.domain.label.entity.LabelGenerateType;

import java.util.List;

public record UploadLabalRequestDto(
        String repoUrl,
        LabelGenerateType labelGenerateType,
        List<LabelListRequestDto> labels
) {
}
