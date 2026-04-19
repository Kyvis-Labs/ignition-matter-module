package com.kyvislabs.matter.client.exception;

public class MatterException extends Exception {
    private final int errorCode;

    public MatterException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static MatterException fromErrorCode(int errorCode, String details) {
        return switch (errorCode) {
            case 1 -> new NodeCommissionFailedException(details);
            case 2 -> new NodeInterviewFailedException(details);
            case 3 -> new NodeNotReadyException(details);
            case 4 -> new NodeNotResolvingException(details);
            case 5 -> new NodeNotExistsException(details);
            case 6 -> new VersionMismatchException(details);
            case 7 -> new SDKStackException(details);
            case 8 -> new InvalidArgumentsException(details);
            case 9 -> new InvalidCommandException(details);
            case 10 -> new UpdateCheckException(details);
            case 11 -> new UpdateException(details);
            default -> new MatterException(errorCode, details);
        };
    }
}
