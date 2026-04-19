package com.kyvislabs.matter.client.exception;

public class NodeNotExistsException extends MatterException {
    public NodeNotExistsException(String message) {
        super(5, message);
    }
}
