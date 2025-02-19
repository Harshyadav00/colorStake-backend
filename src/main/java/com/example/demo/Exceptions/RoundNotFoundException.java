package com.example.demo.Exceptions;

public class RoundNotFoundException extends RuntimeException {

    // Default constructor
    public RoundNotFoundException() {
        super("Bet not found.");
    }

    // Constructor with a custom message
    public RoundNotFoundException(String message) {
        super(message);
    }
}
