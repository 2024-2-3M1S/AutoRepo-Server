package org.autorepo.server.domain.readme.dto.request;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@Getter
@RequiredArgsConstructor
public class TechStack {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("color")
    private final String color;

    @JsonProperty("icon")
    private final String icon;
}

