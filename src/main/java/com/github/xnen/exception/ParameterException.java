package com.github.xnen.exception;

public class ParameterException extends Exception {
    private final int id;

    public ParameterException(int id, String message) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
