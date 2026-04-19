package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class MatterNodeEvent {
    @SerializedName("node_id")
    private int nodeId;

    @SerializedName("endpoint_id")
    private int endpointId;

    @SerializedName("cluster_id")
    private int clusterId;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("event_number")
    private long eventNumber;

    @SerializedName("priority")
    private int priority;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("timestamp_type")
    private int timestampType;

    @SerializedName("data")
    private Map<String, Object> data;

    public int getNodeId() {
        return nodeId;
    }

    public int getEndpointId() {
        return endpointId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public int getEventId() {
        return eventId;
    }

    public long getEventNumber() {
        return eventNumber;
    }

    public int getPriority() {
        return priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTimestampType() {
        return timestampType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MatterNodeEvent{" +
                "nodeId=" + nodeId +
                ", endpointId=" + endpointId +
                ", clusterId=" + clusterId +
                ", eventId=" + eventId +
                '}';
    }
}
