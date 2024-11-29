package org.autorepo.server.global.error.exception;


import org.autorepo.server.global.error.ErrorCode;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(ErrorCode errorCode) {super(errorCode);}
}