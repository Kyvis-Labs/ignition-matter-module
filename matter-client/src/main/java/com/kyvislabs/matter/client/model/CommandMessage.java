package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class CommandMessage {
    @SerializedName("message_id")
    private String messageId;

    @SerializedName("command")
    private String command;

    @SerializedName("args")
    private Map<String, Object> args;

    public CommandMessage(String messageId, String command, Map<String, Object> args) {
        this.messageId = messageId;
        this.command = command;
        this.args = args;
    }

    public CommandMessage(String messageId, String command) {
        this(messageId, command, null);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, Object> getArgs() {
        return args;
    }
}
