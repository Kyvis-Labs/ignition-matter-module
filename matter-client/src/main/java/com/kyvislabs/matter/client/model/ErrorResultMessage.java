package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;

public class ErrorResultMessage {
    @SerializedName("message_id")
    private String messageId;

    @SerializedName("error_code")
    private int errorCode;

    @SerializedName("details")
    private String details;

    public String getMessageId() {
        return messageId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}
