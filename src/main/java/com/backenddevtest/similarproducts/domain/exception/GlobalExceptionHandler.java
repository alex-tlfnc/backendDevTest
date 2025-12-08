package com.backenddevtest.similarproducts.domain.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public Mono<ResponseEntity<ErrorResponse>> handleProductNotFound(ProductNotFoundException exception){
        log.warn("Product not found: {}", exception.getProductId());
        return Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("PRODUCT_NOT_FOUND", exception.getMessage()))
        );
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServiceUnavailable(ExternalServiceUnavailableException ex) {
        log.error("External service unavailable: {}", ex.getMessage());
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ErrorResponse("EXTERNAL_SERVICE_UNAVAILABLE", ex.getMessage()))
        );
    }

    @ExceptionHandler(ExternalServiceTimeoutException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTimeout(ExternalServiceTimeoutException ex) {
        log.error("External service timeout: {}", ex.getMessage());
        return Mono.just(
                ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .body(new ErrorResponse("EXTERNAL_TIMEOUT", ex.getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.toString());
        return Mono.just(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("INTERNAL_ERROR", "Unexpected internal error"))
        );
    }

    record ErrorResponse(String code, String message) {}
}
