package com.selsup.exceptions;

public class SendRequestException extends RuntimeException {
    public SendRequestException(String message) {
        super(message);
    }

    public SendRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}