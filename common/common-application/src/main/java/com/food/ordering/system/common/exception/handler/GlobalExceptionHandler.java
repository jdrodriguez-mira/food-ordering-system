package com.food.ordering.system.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler
    public ResponseEntity<ErrorDTO> handleException(Exception exception){
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message("There is an internal server error.")
                        .build());
    }

    @ResponseBody
    @ExceptionHandler
    public ResponseEntity<ErrorDTO> handleValidationException(ValidationException exception){
        ErrorDTO errorDTO = null;
        if (exception instanceof ConstraintViolationException){
            String violations = extractViolations((ConstraintViolationException) exception);
            log.error(violations, exception);
            ErrorDTO.builder()
                    .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(violations).build();
        } else {
            log.error(exception.getMessage(), exception);
            ErrorDTO.builder()
                    .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(exception.getMessage()).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDTO);
    }

    private String extractViolations(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }
}
