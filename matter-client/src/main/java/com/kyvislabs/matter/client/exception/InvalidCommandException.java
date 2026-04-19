package com.kyvislabs.matter.client.exception;

public class InvalidCommandException extends MatterException {
    public InvalidCommandException(String message) {
        super(9, message);
    }
}
