package com.knowledgehub.exception;

import com.knowledgehub.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理已明确约定的业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex) {

        if ("USERNAME_ALREADY_EXISTS".equals(ex.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
        }
        if ("AUTHENTICATION_FAILED".equals(ex.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
        }
        if ("CATEGORY_ALREADY_EXISTS".equals(ex.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
        }
        if ("CATEGORY_NOT_FOUND".equals(ex.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
        }

        if ("INVALID_CATEGORY_NAME".equals(ex.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
        }
        // 未配置响应规则的业务异常，不能随意当成客户端错误
        log.error("未配置业务异常映射，code={}", ex.getCode(), ex);

        return internalError();
    }

    // 处理 @Valid 触发的 DTO 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(text -> text != null)
                .findFirst()
                .orElse("请求参数不符合要求");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    // 处理 JSON 解析失败、请求体缺失等情况
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "INVALID_REQUEST_BODY",
                        "请求体缺失或JSON格式不正确"
                ));
    }

    // 兜底处理未预料的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex) {

        log.error("处理请求时发生未预料的异常", ex);

        return internalError();
    }

    private ResponseEntity<ErrorResponse> internalError() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "服务器内部错误，请稍后重试"
                ));
    }
}