package org.autorepo.server.domain.readme.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReadmeRequest {
    private String title;
    private String description;
    private List<TechStack> stack;
    private List<TeamMember> teamMembers;
    private String installation;
}
