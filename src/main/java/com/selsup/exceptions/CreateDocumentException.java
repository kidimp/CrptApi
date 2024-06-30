package com.selsup.exceptions;

public class CreateDocumentException extends RuntimeException {
    public CreateDocumentException(String message) {
        super(message);
    }

    public CreateDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}