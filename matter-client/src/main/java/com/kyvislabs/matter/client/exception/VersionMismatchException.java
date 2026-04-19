package com.kyvislabs.matter.client.exception;

public class VersionMismatchException extends MatterException {
    public VersionMismatchException(String message) {
        super(6, message);
    }
}
