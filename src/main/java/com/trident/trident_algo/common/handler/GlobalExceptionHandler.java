package com.trident.trident_algo.common.handler;

import com.trident.trident_algo.common.helper.PayloadWrapperHelper;
import com.trident.trident_algo.common.model.GenericPayloadWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericPayloadWrapper<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PayloadWrapperHelper.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
