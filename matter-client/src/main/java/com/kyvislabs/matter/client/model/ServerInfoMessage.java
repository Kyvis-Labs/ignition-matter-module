package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;

public class ServerInfoMessage {
    @SerializedName("fabric_id")
    private long fabricId;

    @SerializedName("compressed_fabric_id")
    private long compressedFabricId;

    @SerializedName("schema_version")
    private int schemaVersion;

    @SerializedName("min_supported_schema_version")
    private int minSupportedSchemaVersion;

    @SerializedName("sdk_version")
    private String sdkVersion;

    @SerializedName("wifi_credentials_set")
    private boolean wifiCredentialsSet;

    @SerializedName("thread_credentials_set")
    private boolean threadCredentialsSet;

    @SerializedName("bluetooth_enabled")
    private boolean bluetoothEnabled;

    public long getFabricId() {
        return fabricId;
    }

    public long getCompressedFabricId() {
        return compressedFabricId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public int getMinSupportedSchemaVersion() {
        return minSupportedSchemaVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public boolean isWifiCredentialsSet() {
        return wifiCredentialsSet;
    }

    public boolean isThreadCredentialsSet() {
        return threadCredentialsSet;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothEnabled;
    }

    @Override
    public String toString() {
        return "ServerInfoMessage{" +
                "fabricId=" + fabricId +
                ", compressedFabricId=" + compressedFabricId +
                ", schemaVersion=" + schemaVersion +
                ", sdkVersion='" + sdkVersion + '\'' +
                ", bluetoothEnabled=" + bluetoothEnabled +
                '}';
    }
}
