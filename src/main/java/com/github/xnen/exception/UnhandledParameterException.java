package com.github.xnen.exception;

public class UnhandledParameterException extends Exception {
    private final int id;

    public UnhandledParameterException(int id, String message) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
