package com.backenddevtest.similarproducts.domain.exception;

public class ExternalServiceTimeoutException extends RuntimeException {

    public ExternalServiceTimeoutException(){
        super("External Service Timeout");
    }

    public ExternalServiceTimeoutException(String message){
        super(message);
    }
}
