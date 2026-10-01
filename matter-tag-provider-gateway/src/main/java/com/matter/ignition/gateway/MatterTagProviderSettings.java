package com.matter.ignition.gateway;

import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.DefaultValue;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Description;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.FormCategory;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.FormField;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Label;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.annotations.Required;
import com.inductiveautomation.ignition.gateway.secrets.SecretConfig;
import com.inductiveautomation.ignition.gateway.web.nav.FormFieldType;

/**
 * Connection settings for a Matter tag provider.
 *
 * <p>Works against either Matter server implementation: matterjs-server (preferred) or the older
 * python-matter-server, whose WebSocket API matterjs-server reimplements.
 *
 * <p>The Wi-Fi and Thread credentials are optional and used only when commissioning new devices.
 * They live here rather than in tags so they are not readable from the tag tree, and they are typed
 * as {@link SecretConfig} so the gateway stores them encrypted — either inline or as a reference to
 * a configured secret provider.
 */
public record MatterTagProviderSettings(
        @FormCategory("CONNECTION")
        @Label("Server URL")
        @FormField(FormFieldType.TEXT)
        @DefaultValue("ws://localhost:5580/ws")
        @Required
        @Description("WebSocket URL of the Matter server — matterjs-server or python-matter-server "
                + "(e.g. ws://192.168.1.100:5580/ws)")
        String serverUrl,

        @FormCategory("COMMISSIONING")
        @Label("Wi-Fi SSID")
        @FormField(FormFieldType.TEXT)
        @Description("Optional. Pushed to the server on connect so BLE commissioning can join "
                + "Wi-Fi devices to this network.")
        String wifiSsid,

        @FormCategory("COMMISSIONING")
        @Label("Wi-Fi Password")
        @Description("Optional. Only used when commissioning Wi-Fi devices.")
        SecretConfig wifiPassword,

        @FormCategory("COMMISSIONING")
        @Label("Thread Dataset")
        @Description("Optional hex-encoded Thread operational dataset, used when commissioning "
                + "Thread devices. Note that matterjs-server cannot commission Thread devices "
                + "directly yet — use multi-admin from an existing ecosystem instead.")
        SecretConfig threadDataset
) {
    public static final MatterTagProviderSettings DEFAULT =
            new MatterTagProviderSettings("ws://localhost:5580/ws", null, null, null);

    public MatterTagProviderSettings {
        if (serverUrl == null) {
            serverUrl = "ws://localhost:5580/ws";
        }
    }

    boolean hasWifiSsid() {
        return wifiSsid != null && !wifiSsid.isBlank();
    }
}
