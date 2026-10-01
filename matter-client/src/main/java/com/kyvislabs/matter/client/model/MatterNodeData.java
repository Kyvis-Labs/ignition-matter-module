package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class MatterNodeData {
    @SerializedName("node_id")
    private long nodeId;

    @SerializedName("date_commissioned")
    private String dateCommissioned;

    @SerializedName("last_interview")
    private String lastInterview;

    @SerializedName("interview_version")
    private int interviewVersion;

    @SerializedName("available")
    private boolean available;

    @SerializedName("is_bridge")
    private boolean isBridge;

    @SerializedName("attributes")
    private Map<String, Object> attributes;

    public long getNodeId() {
        return nodeId;
    }

    public String getDateCommissioned() {
        return dateCommissioned;
    }

    public String getLastInterview() {
        return lastInterview;
    }

    public int getInterviewVersion() {
        return interviewVersion;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isBridge() {
        return isBridge;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String path) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(path);
    }

    @Override
    public String toString() {
        return "MatterNodeData{" +
                "nodeId=" + Long.toUnsignedString(nodeId) +
                ", available=" + available +
                ", isBridge=" + isBridge +
                ", attributeCount=" + (attributes != null ? attributes.size() : 0) +
                '}';
    }
}
