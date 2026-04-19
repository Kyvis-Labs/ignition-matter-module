package com.kyvislabs.matter.client.model;

import com.google.gson.annotations.SerializedName;

public class MatterSoftwareVersion {
    @SerializedName("vid")
    private int vid;

    @SerializedName("pid")
    private int pid;

    @SerializedName("software_version")
    private int softwareVersion;

    @SerializedName("software_version_string")
    private String softwareVersionString;

    @SerializedName("firmware_information")
    private String firmwareInformation;

    @SerializedName("min_applicable_software_version")
    private int minApplicableSoftwareVersion;

    @SerializedName("max_applicable_software_version")
    private int maxApplicableSoftwareVersion;

    @SerializedName("release_notes_url")
    private String releaseNotesUrl;

    @SerializedName("update_source")
    private String updateSource;

    public int getVid() {
        return vid;
    }

    public int getPid() {
        return pid;
    }

    public int getSoftwareVersion() {
        return softwareVersion;
    }

    public String getSoftwareVersionString() {
        return softwareVersionString;
    }

    public String getFirmwareInformation() {
        return firmwareInformation;
    }

    public int getMinApplicableSoftwareVersion() {
        return minApplicableSoftwareVersion;
    }

    public int getMaxApplicableSoftwareVersion() {
        return maxApplicableSoftwareVersion;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public String getUpdateSource() {
        return updateSource;
    }

    @Override
    public String toString() {
        return "MatterSoftwareVersion{" +
                "vid=" + vid +
                ", pid=" + pid +
                ", softwareVersion=" + softwareVersion +
                ", softwareVersionString='" + softwareVersionString + '\'' +
                '}';
    }
}
