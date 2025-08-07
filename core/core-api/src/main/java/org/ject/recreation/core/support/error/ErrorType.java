package org.ject.recreation.core.support.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

public enum ErrorType {

    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.",
            LogLevel.ERROR),
    GAME_ALREADY_EXISTS(HttpStatus.CONFLICT, ErrorCode.E409, "해당 UUID를 사용하는 게임이 이미 존재합니다.", LogLevel.WARN),
    GAME_IS_UPDATED(HttpStatus.CONFLICT, ErrorCode.E409, "해당 게임의 정보가 변경되었습니다.", LogLevel.WARN),
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "해당 게임이 존재하지 않습니다.", LogLevel.WARN),
    GAME_IS_DELETED(HttpStatus.NOT_FOUND, ErrorCode.E404, "삭제된 게임입니다.", LogLevel.WARN),
    GAME_FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "해당 게임에 대한 권한이 없습니다.", LogLevel.WARN),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "로그인이 필요합니다.", LogLevel.WARN),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ErrorCode.E400, "요청값이 유효하지 않습니다.", LogLevel.WARN);

    private final HttpStatus status;

    private final ErrorCode code;

    private final String message;

    private final LogLevel logLevel;

    ErrorType(HttpStatus status, ErrorCode code, String message, LogLevel logLevel) {

        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

}

