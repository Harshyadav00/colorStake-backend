package com.example.demo.Exceptions;

public class AmountNotFoundException extends RuntimeException {

    public AmountNotFoundException() {
        super("Not Enough Balance");
    }

    public AmountNotFoundException(String message) {
        super(message);
    }
}
