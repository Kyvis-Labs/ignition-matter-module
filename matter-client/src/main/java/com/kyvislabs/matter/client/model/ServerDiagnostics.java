package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ServerDiagnostics {
    @SerializedName("info")
    private ServerInfoMessage info;

    @SerializedName("nodes")
    private List<MatterNodeData> nodes;

    @SerializedName("events")
    private List<Map<String, Object>> events;

    public ServerInfoMessage getInfo() {
        return info;
    }

    public List<MatterNodeData> getNodes() {
        return nodes;
    }

    public List<Map<String, Object>> getEvents() {
        return events;
    }
}
