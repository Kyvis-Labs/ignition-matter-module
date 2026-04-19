package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommissionableNodeData {
    @SerializedName("instance_name")
    private String instanceName;

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("port")
    private Integer port;

    @SerializedName("long_discriminator")
    private Integer longDiscriminator;

    @SerializedName("vendor_id")
    private Integer vendorId;

    @SerializedName("product_id")
    private Integer productId;

    @SerializedName("commissioning_mode")
    private Integer commissioningMode;

    @SerializedName("device_type")
    private Integer deviceType;

    @SerializedName("device_name")
    private String deviceName;

    @SerializedName("pairing_instruction")
    private String pairingInstruction;

    @SerializedName("pairing_hint")
    private Integer pairingHint;

    @SerializedName("mrp_retry_interval_idle")
    private Integer mrpRetryIntervalIdle;

    @SerializedName("mrp_retry_interval_active")
    private Integer mrpRetryIntervalActive;

    @SerializedName("supports_tcp")
    private Boolean supportsTcp;

    @SerializedName("addresses")
    private List<String> addresses;

    @SerializedName("rotating_id")
    private String rotatingId;

    public String getInstanceName() {
        return instanceName;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getLongDiscriminator() {
        return longDiscriminator;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getCommissioningMode() {
        return commissioningMode;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPairingInstruction() {
        return pairingInstruction;
    }

    public Integer getPairingHint() {
        return pairingHint;
    }

    public Boolean getSupportsTcp() {
        return supportsTcp;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public String getRotatingId() {
        return rotatingId;
    }

    @Override
    public String toString() {
        return "CommissionableNodeData{" +
                "deviceName='" + deviceName + '\'' +
                ", vendorId=" + vendorId +
                ", productId=" + productId +
                '}';
    }
}
