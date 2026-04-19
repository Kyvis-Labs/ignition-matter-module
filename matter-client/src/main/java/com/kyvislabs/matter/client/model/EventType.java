package com.kyvislabs.matter.client.model;

public enum EventType {
    NODE_ADDED("node_added"),
    NODE_UPDATED("node_updated"),
    NODE_REMOVED("node_removed"),
    NODE_EVENT("node_event"),
    ATTRIBUTE_UPDATED("attribute_updated"),
    SERVER_SHUTDOWN("server_shutdown"),
    SERVER_INFO_UPDATED("server_info_updated"),
    ENDPOINT_ADDED("endpoint_added"),
    ENDPOINT_REMOVED("endpoint_removed");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventType fromValue(String value) {
        for (EventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
