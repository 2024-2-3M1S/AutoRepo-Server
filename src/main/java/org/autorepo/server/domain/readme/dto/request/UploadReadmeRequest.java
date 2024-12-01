package org.autorepo.server.domain.readme.dto.request;

public record UploadReadmeRequest(
        String repoUrl,
        String content
) {
}

