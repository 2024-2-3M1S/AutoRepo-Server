package org.autorepo.server.global.error.exception;

import org.autorepo.server.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }

    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
