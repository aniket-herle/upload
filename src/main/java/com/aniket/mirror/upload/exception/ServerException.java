package com.aniket.mirror.upload.exception;

import org.springframework.http.HttpStatus;

public class ServerException extends AppException {
  public ServerException(ErrorCode errorCode, String message) {
    super(errorCode, getStatusForServerError(errorCode), message);
  }

  public ServerException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, getStatusForServerError(errorCode), message, cause);
  }

  private static HttpStatus getStatusForServerError(ErrorCode errorCode) {
    return switch (errorCode) {
      case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
      case EXTERNAL_SERVICE_FAILURE -> HttpStatus.BAD_GATEWAY;
      default -> HttpStatus.INTERNAL_SERVER_ERROR; // fallback
    };
  }
}