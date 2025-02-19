package com.example.demo.Exceptions;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Email Already Registered");
    }

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
