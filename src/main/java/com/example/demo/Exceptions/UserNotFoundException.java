package com.example.demo.Exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {
        super("User Not Found");
    }

    // Constructor with a custom message
    public UserNotFoundException(String message) {
        super(message);
    }
}
