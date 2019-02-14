package com.upgrade.codechallenge.exception;

public class OccupiedDateRangeException extends  RuntimeException {

    private static final long serialVersionUID = 1L;

    public OccupiedDateRangeException(String message) {
        super(message);
    }
}