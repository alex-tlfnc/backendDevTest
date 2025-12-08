package com.backenddevtest.similarproducts.domain.exception;

public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(){
        super("External Service Unavailable");
    }

    public ExternalServiceUnavailableException(String message){
        super(message);
    }
}
