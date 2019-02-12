package com.upgrade.codechallenge.exception;

public class OcuppedDateRangeException extends  RuntimeException {

    private static final long serialVersionUID = 1L;

    public OcuppedDateRangeException(String message) {
        super(message);
    }
}