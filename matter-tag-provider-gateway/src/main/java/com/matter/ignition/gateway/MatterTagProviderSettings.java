package com.matter.ignition.gateway;

import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.DefaultValue;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Description;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.FormCategory;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.FormField;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Label;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Required;
import com.inductiveautomation.ignition.gateway.web.nav.FormFieldType;

public record MatterTagProviderSettings(
        @FormCategory("CONNECTION")
        @Label("Server URL")
        @FormField(FormFieldType.TEXT)
        @DefaultValue("ws://localhost:5580/ws")
        @Required
        @Description("WebSocket URL of the python-matter-server (e.g. ws://192.168.1.100:5580/ws)")
        String serverUrl
) {
    public static final MatterTagProviderSettings DEFAULT =
            new MatterTagProviderSettings("ws://localhost:5580/ws");

    public MatterTagProviderSettings {
        if (serverUrl == null) {
            serverUrl = "ws://localhost:5580/ws";
        }
    }
}
