package org.autorepo.server.global.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    /**
     * 400 Bad Request
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    USER_ID_MISSING(HttpStatus.BAD_REQUEST, "토큰에서 사용자 ID를 찾을 수 없습니다."),

    /**
     * 401 Unauthorized
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "리소스 접근 권한이 없습니다."),
    JWT_UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, "사용자의 로그인 검증을 실패했습니다."),

    /**
     * 403 Forbidden
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "리소스 접근 권한이 없습니다."),

    /**
     * 404 Not Found
     */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    REPO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 레포지토리를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 유저를 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레쉬 토큰을 찾을 수 없습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 템플릿을 찾을 수 없습니다."),
    README_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 README를 찾을 수 없습니다."),

    /**
     * 405 Method Not Allowed
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "잘못된 HTTP method 요청입니다."),
    UNSUPPORTED_OPERATION(HttpStatus.METHOD_NOT_ALLOWED, "지원되지 않는 작업입니다."),

    /**
     * 409 Conflict
     */
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),

    /**
     * 422 Unprocessable Entity
     */
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "요청 처리에 실패했습니다."),

    /**
     * 500 Internal Server Error
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    REPO_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "레포지토리 파싱 중 오류가 발생했습니다."),
    GITHUB_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GitHub API 호출 중 오류가 발생했습니다."),
    GITHUB_LABEL_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "깃허브 기존 라벨 삭제 중 오류가 발생했습니다."),
    GITHUB_LABEL_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "깃허브 라벨 생성 중 오류가 발생했습니다."),
    GITHUB_TEMPLATE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "깃허브 템플릿 업로드 중 오류가 발생했습니다."),
    LABEL_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "라벨 삭제 중 오류가 발생했습니다."),
    README_GENERATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "리드미 생성 중 오류가 발생했습니다."),
    GITHUB_README_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "리드미 업로드 중 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
