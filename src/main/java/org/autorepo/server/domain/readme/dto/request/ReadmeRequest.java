package org.autorepo.server.domain.readme.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReadmeRequest {
    private String title;
    private String description;
    private List<String> stack;
    private List<String> teamMembers;
    private String installation;

}
