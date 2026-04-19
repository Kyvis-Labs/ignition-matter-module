package com.kyvislabs.matter.client.exception;

public class NodeCommissionFailedException extends MatterException {
    public NodeCommissionFailedException(String message) {
        super(1, message);
    }
}
