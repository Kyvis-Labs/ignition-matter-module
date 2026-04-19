package com.kyvislabs.matter.client.exception;

public class UpdateCheckException extends MatterException {
    public UpdateCheckException(String message) {
        super(10, message);
    }
}
