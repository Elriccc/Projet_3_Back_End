package com.openclassrooms.datashare.handler;

public class ExpiredLinkException extends RuntimeException {
    public ExpiredLinkException(String message) {
        super(message);
    }
}
