package com.kyvislabs.matter.client.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class SuccessResultMessage {
    @SerializedName("message_id")
    private String messageId;

    @SerializedName("result")
    private JsonElement result;

    public String getMessageId() {
        return messageId;
    }

    public JsonElement getResult() {
        return result;
    }
}
