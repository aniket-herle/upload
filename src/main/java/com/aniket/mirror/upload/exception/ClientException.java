package com.aniket.mirror.upload.exception;

import org.springframework.http.HttpStatus;

public class ClientException extends AppException {
  public ClientException(ErrorCode errorCode, String message) {
    super(errorCode, getStatusForClientError(errorCode), message);
  }

  public ClientException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, getStatusForClientError(errorCode), message, cause);
  }

  private static HttpStatus getStatusForClientError(ErrorCode errorCode) {
    return switch (errorCode) {
      case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
      case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case CONFLICT -> HttpStatus.CONFLICT;
      case FORBIDDEN -> HttpStatus.FORBIDDEN;
      default -> HttpStatus.BAD_REQUEST; // fallback
    };
  }
}
