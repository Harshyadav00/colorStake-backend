package com.example.demo.Exceptions;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Invalid Password");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}
