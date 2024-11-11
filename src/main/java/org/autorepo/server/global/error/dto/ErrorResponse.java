package org.autorepo.server.global.error.dto;

import org.autorepo.server.global.error.ErrorCode;

public record ErrorResponse(int status, String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.getMessage());
    }
}
