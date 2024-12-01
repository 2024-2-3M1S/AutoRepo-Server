package org.autorepo.server.domain.repo.dto.response;

public record RepoResponse(
        String repoName,
        String repoUrl,
        Long userId
) {

}
