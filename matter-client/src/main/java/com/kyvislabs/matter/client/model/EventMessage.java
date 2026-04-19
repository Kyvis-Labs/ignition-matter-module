package com.kyvislabs.matter.client.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class EventMessage {
    @SerializedName("event")
    private String event;

    @SerializedName("data")
    private JsonElement data;

    public String getEvent() {
        return event;
    }

    public EventType getEventType() {
        return EventType.fromValue(event);
    }

    public JsonElement getData() {
        return data;
    }
}
