package com.example.demo.Entity.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Colour {
    RED,
    BLUE;


    @JsonCreator
    public static Colour fromString(String value) {
        for (Colour status : Colour.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid Colour: " + value);
    }
}
