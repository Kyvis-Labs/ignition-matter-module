package com.matter.ignition.gateway;

import com.kyvislabs.matter.client.MatterClient;
import com.kyvislabs.matter.client.model.CommissionableNodeData;
import com.kyvislabs.matter.client.model.CommissioningParameters;
import com.kyvislabs.matter.client.model.MatterNodeData;
import com.kyvislabs.matter.client.model.MatterSoftwareVersion;
import com.kyvislabs.matter.client.model.ServerInfoMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway-scope implementation of {@code system.matter.*}: commissioning and node management for a
 * Matter tag provider, which the tag tree alone cannot express.
 *
 * <p>Node IDs are unsigned 64-bit. Jython has arbitrary-precision integers, but a Java {@code long}
 * does not, so every ID crosses this boundary as a decimal <em>string</em> and is parsed with
 * {@link Long#parseUnsignedLong(String)}. Returning them as strings keeps matterjs-server's test
 * node IDs (at and above {@code 0xFFFF_FFFE_0000_0000}) readable instead of negative.
 *
 * <p>Scope: gateway only, which covers Perspective, gateway event scripts and tag event scripts.
 * Vision and Designer callers would need an RPC implementation on the module hook.
 */
public class MatterScriptModule {

    // ---- Discovery of providers and state ----

    /** Names of the Matter tag providers currently running on this gateway. */
    public List<String> getProviders() {
        return MatterProviderRegistry.names();
    }

    /** Handshake data for a provider's server: fabric IDs, schema version, SDK version. */
    public Map<String, Object> getServerInfo(String provider) {
        ServerInfoMessage info = client(provider).getServerInfo();
        Map<String, Object> out = new LinkedHashMap<>();
        if (info == null) {
            return out;
        }
        out.put("fabricId", String.valueOf(info.getFabricId()));
        out.put("compressedFabricId", String.valueOf(info.getCompressedFabricId()));
        out.put("schemaVersion", info.getSchemaVersion());
        out.put("minSupportedSchemaVersion", info.getMinSupportedSchemaVersion());
        out.put("sdkVersion", info.getSdkVersion());
        out.put("wifiCredentialsSet", info.isWifiCredentialsSet());
        out.put("threadCredentialsSet", info.isThreadCredentialsSet());
        out.put("bluetoothEnabled", info.isBluetoothEnabled());
        return out;
    }

    /** Every node known to the provider, newest interview data included. */
    public List<Map<String, Object>> getNodes(String provider) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (MatterNodeData node : client(provider).getNodes().values()) {
            out.add(describe(node));
        }
        return out;
    }

    public Map<String, Object> getNode(String provider, Object nodeId) {
        return describe(client(provider).getNode(parseNodeId(nodeId)));
    }

    // ---- Commissioning ----

    /**
     * Pairs a device using an 11-digit manual pairing code or a QR payload.
     *
     * @param networkOnly skip BLE and pair only over IP, for a device already on the network
     * @return the new node's ID, as a decimal string
     */
    public String commissionWithCode(String provider, String code, boolean networkOnly)
            throws Exception {
        MatterNodeData node = client(provider).commissionWithCode(code, networkOnly);
        return Long.toUnsignedString(node.getNodeId());
    }

    public String commissionWithCode(String provider, String code) throws Exception {
        return commissionWithCode(provider, code, false);
    }

    /** Pairs a device already reachable on the network by its setup PIN. */
    public String commissionOnNetwork(String provider, int setupPinCode, String ipAddress)
            throws Exception {
        MatterNodeData node = client(provider).commissionOnNetwork(setupPinCode, ipAddress);
        return Long.toUnsignedString(node.getNodeId());
    }

    public String commissionOnNetwork(String provider, int setupPinCode) throws Exception {
        return commissionOnNetwork(provider, setupPinCode, null);
    }

    /**
     * Opens a commissioning window on an already-paired node so another ecosystem can also control
     * it (Matter multi-admin). This is also the way to bring in a Thread device that matterjs-server
     * cannot currently commission directly: pair it elsewhere first, then share it here.
     *
     * @return {@code setupPinCode}, {@code setupManualCode} and {@code setupQrCode}
     */
    public Map<String, Object> openCommissioningWindow(String provider, Object nodeId, int timeout)
            throws Exception {
        CommissioningParameters params =
                client(provider).openCommissioningWindow(parseNodeId(nodeId), timeout, 1000, 1, null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("setupPinCode", params.getSetupPinCode());
        out.put("setupManualCode", params.getSetupManualCode());
        out.put("setupQrCode", params.getSetupQrCode());
        return out;
    }

    public Map<String, Object> openCommissioningWindow(String provider, Object nodeId)
            throws Exception {
        CommissioningParameters params =
                client(provider).openCommissioningWindow(parseNodeId(nodeId));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("setupPinCode", params.getSetupPinCode());
        out.put("setupManualCode", params.getSetupManualCode());
        out.put("setupQrCode", params.getSetupQrCode());
        return out;
    }

    /** Devices currently advertising themselves as commissionable on the network. */
    public List<Map<String, Object>> discover(String provider) throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CommissionableNodeData node : client(provider).discoverCommissionableNodes()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("instanceName", node.getInstanceName());
            entry.put("hostName", node.getHostName());
            entry.put("deviceName", node.getDeviceName());
            entry.put("vendorId", node.getVendorId());
            entry.put("productId", node.getProductId());
            entry.put("commissioningMode", node.getCommissioningMode());
            entry.put("addresses", node.getAddresses());
            entry.put("pairingInstruction", node.getPairingInstruction());
            out.add(entry);
        }
        return out;
    }

    /**
     * Removes a node from this fabric. The device stops responding to this gateway and must be
     * re-commissioned to come back, so this is deliberately scripting-only — there is no command
     * tag for it.
     */
    public void removeNode(String provider, Object nodeId) throws Exception {
        client(provider).removeNode(parseNodeId(nodeId));
    }

    /** Stores Wi-Fi credentials on the server for use by subsequent BLE commissioning. */
    public void setWifiCredentials(String provider, String ssid, String password) throws Exception {
        client(provider).setWifiCredentials(ssid, password);
    }

    /** Stores a Thread operational dataset (hex) for use by subsequent BLE commissioning. */
    public void setThreadDataset(String provider, String dataset) throws Exception {
        client(provider).setThreadDataset(dataset);
    }

    public void setFabricLabel(String provider, String label) throws Exception {
        client(provider).setDefaultFabricLabel(label);
    }

    // ---- Node operations ----

    public void interviewNode(String provider, Object nodeId) throws Exception {
        client(provider).interviewNode(parseNodeId(nodeId));
    }

    public Map<String, Boolean> pingNode(String provider, Object nodeId) throws Exception {
        return client(provider).pingNode(parseNodeId(nodeId));
    }

    public List<String> getNodeIpAddresses(String provider, Object nodeId) throws Exception {
        return client(provider).getNodeIpAddresses(parseNodeId(nodeId));
    }

    /** @return the available update, or an empty map when the node is already current */
    public Map<String, Object> checkNodeUpdate(String provider, Object nodeId) throws Exception {
        MatterSoftwareVersion update = client(provider).checkNodeUpdate(parseNodeId(nodeId));
        Map<String, Object> out = new LinkedHashMap<>();
        if (update == null) {
            return out;
        }
        out.put("softwareVersion", update.getSoftwareVersion());
        out.put("softwareVersionString", update.getSoftwareVersionString());
        out.put("updateSource", update.getUpdateSource());
        return out;
    }

    public void updateNode(String provider, Object nodeId, Object softwareVersion) throws Exception {
        long id = parseNodeId(nodeId);
        if (softwareVersion instanceof Number n) {
            client(provider).updateNode(id, n.intValue());
        } else {
            client(provider).updateNode(id, String.valueOf(softwareVersion));
        }
    }

    /** Invokes a Matter cluster command, e.g. {@code Identify} or {@code Toggle}. */
    public Object sendDeviceCommand(String provider, Object nodeId, int endpointId, int clusterId,
                                    String commandName, Map<String, Object> payload) throws Exception {
        var result = client(provider).sendDeviceCommand(
                parseNodeId(nodeId), endpointId, clusterId, commandName,
                payload == null ? Map.of() : payload);
        return result == null ? null : result.toString();
    }

    /** Reads an attribute by numeric Matter path, e.g. {@code "1/6/0"}. */
    public Map<String, Object> readAttribute(String provider, Object nodeId, String attributePath)
            throws Exception {
        return client(provider).readAttribute(parseNodeId(nodeId), attributePath);
    }

    /** Writes an attribute by numeric Matter path. Tag writes are usually easier than this. */
    public void writeAttribute(String provider, Object nodeId, String attributePath, Object value)
            throws Exception {
        client(provider).writeAttribute(parseNodeId(nodeId), attributePath, value);
    }

    /** Server-side diagnostics dump: server info, nodes and recent events. */
    public Object getDiagnostics(String provider) throws Exception {
        var diagnostics = client(provider).getDiagnostics();
        return diagnostics == null ? null : diagnostics.toString();
    }

    // ---- Internals ----

    private MatterClient client(String provider) {
        return MatterProviderRegistry.require(provider).requireClient();
    }

    /**
     * Accepts a node ID as a string or any integer type. Strings are parsed unsigned, so an ID
     * above {@link Long#MAX_VALUE} round-trips as the right bit pattern.
     */
    private static long parseNodeId(Object nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("nodeId is required");
        }
        if (nodeId instanceof Number n) {
            return n.longValue();
        }
        String text = String.valueOf(nodeId).trim();
        try {
            return Long.parseUnsignedLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid node ID: " + text, e);
        }
    }

    private static Map<String, Object> describe(MatterNodeData node) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (node == null) {
            return out;
        }
        out.put("nodeId", Long.toUnsignedString(node.getNodeId()));
        out.put("available", node.isAvailable());
        out.put("isBridge", node.isBridge());
        out.put("dateCommissioned", node.getDateCommissioned());
        out.put("lastInterview", node.getLastInterview());
        out.put("interviewVersion", node.getInterviewVersion());
        out.put("attributeCount", node.getAttributes() == null ? 0 : node.getAttributes().size());
        return out;
    }
}
