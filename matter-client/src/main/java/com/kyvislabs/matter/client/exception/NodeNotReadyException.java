package com.kyvislabs.matter.client.exception;

public class NodeNotReadyException extends MatterException {
    public NodeNotReadyException(String message) {
        super(3, message);
    }
}
