package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;

public class CommissioningParameters {
    @SerializedName("setup_pin_code")
    private int setupPinCode;

    @SerializedName("setup_manual_code")
    private String setupManualCode;

    @SerializedName("setup_qr_code")
    private String setupQrCode;

    public int getSetupPinCode() {
        return setupPinCode;
    }

    public String getSetupManualCode() {
        return setupManualCode;
    }

    public String getSetupQrCode() {
        return setupQrCode;
    }

    @Override
    public String toString() {
        return "CommissioningParameters{" +
                "setupPinCode=" + setupPinCode +
                ", setupManualCode='" + setupManualCode + '\'' +
                '}';
    }
}
