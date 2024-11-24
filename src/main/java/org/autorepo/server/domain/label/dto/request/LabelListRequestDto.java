package org.autorepo.server.domain.label.dto.request;

public record LabelListRequestDto(
        String labelName,
        String color,
        String description
) {
}
