package com.kyvislabs.matter.client.event;

import com.google.gson.JsonElement;
import com.kyvislabs.matter.client.model.EventType;

@FunctionalInterface
public interface MatterEventListener {
    void onEvent(EventType eventType, JsonElement data);
}
