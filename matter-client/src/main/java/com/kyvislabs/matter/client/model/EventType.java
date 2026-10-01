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
    ENDPOINT_REMOVED("endpoint_removed"),
    // matterjs-server only; both are opt-in, so they arrive only if we ask for them.
    THREAD_DIAGNOSTICS_UPDATED("thread_diagnostics_updated"),
    NETWORK_TOPOLOGY_UPDATED("network_topology_updated");

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

    /**
     * Lookup that tolerates event types this client does not know about. A newer server may
     * introduce events at any time, and an unknown one must not take down message handling.
     */
    public static EventType fromValueOrNull(String value) {
        for (EventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
