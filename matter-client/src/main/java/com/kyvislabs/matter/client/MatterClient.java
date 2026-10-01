package com.kyvislabs.matter.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kyvislabs.matter.client.event.MatterEventListener;
import com.kyvislabs.matter.client.exception.ConnectionException;
import com.kyvislabs.matter.client.exception.MatterException;
import com.kyvislabs.matter.client.model.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatterClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatterClient.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    private final Gson gson = MatterJson.gson();
    private final MatterClientConnection connection;
    private final AtomicLong messageIdCounter = new AtomicLong(0);
    private final ConcurrentHashMap<String, CompletableFuture<JsonElement>> pendingRequests = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<MatterEventListener> eventListeners = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Long, MatterNodeData> nodes = new ConcurrentHashMap<>();

    private long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

    public MatterClient(String wsServerUrl) {
        this(URI.create(wsServerUrl));
    }

    public MatterClient(URI wsServerUri) {
        this.connection = new MatterClientConnection(wsServerUri);
    }

    // -- Connection lifecycle --

    public ServerInfoMessage connect() throws Exception {
        return connection.connect(this::handleIncomingMessage, this::handleDisconnect)
                .get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void disconnect() {
        for (CompletableFuture<JsonElement> future : pendingRequests.values()) {
            future.cancel(true);
        }
        pendingRequests.clear();
        connection.disconnect();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public ServerInfoMessage getServerInfo() {
        return connection.getServerInfo();
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    // -- Event subscription --

    public void addEventListener(MatterEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(MatterEventListener listener) {
        eventListeners.remove(listener);
    }

    // -- Node access --

    public Map<Long, MatterNodeData> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public MatterNodeData getNode(long nodeId) {
        return nodes.get(nodeId);
    }

    // -- API Commands --

    public List<MatterNodeData> startListening() throws Exception {
        JsonElement result = sendCommand(APICommand.START_LISTENING).get(timeoutSeconds, TimeUnit.SECONDS);
        List<MatterNodeData> nodeList = gson.fromJson(result,
                new TypeToken<List<MatterNodeData>>() {}.getType());
        nodes.clear();
        for (MatterNodeData node : nodeList) {
            nodes.put(node.getNodeId(), node);
        }
        return nodeList;
    }

    public MatterNodeData commissionWithCode(String code, boolean networkOnly) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("code", code);
        args.put("network_only", networkOnly);
        JsonElement result = sendCommand(APICommand.COMMISSION_WITH_CODE, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, MatterNodeData.class);
    }

    public MatterNodeData commissionWithCode(String code) throws Exception {
        return commissionWithCode(code, false);
    }

    public MatterNodeData commissionOnNetwork(int setupPinCode, String ipAddr) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("setup_pin_code", setupPinCode);
        if (ipAddr != null) {
            args.put("ip_addr", ipAddr);
        }
        JsonElement result = sendCommand(APICommand.COMMISSION_ON_NETWORK, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, MatterNodeData.class);
    }

    public MatterNodeData commissionOnNetwork(int setupPinCode) throws Exception {
        return commissionOnNetwork(setupPinCode, null);
    }

    public void setWifiCredentials(String ssid, String credentials) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("ssid", ssid);
        args.put("credentials", credentials);
        sendCommand(APICommand.SET_WIFI_CREDENTIALS, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void setThreadDataset(String dataset) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("dataset", dataset);
        sendCommand(APICommand.SET_THREAD_DATASET, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public CommissioningParameters openCommissioningWindow(long nodeId, int timeout,
                                                           int iteration, int option,
                                                           Integer discriminator) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("timeout", timeout);
        args.put("iteration", iteration);
        args.put("option", option);
        if (discriminator != null) {
            args.put("discriminator", discriminator);
        }
        JsonElement result = sendCommand(APICommand.OPEN_COMMISSIONING_WINDOW, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, CommissioningParameters.class);
    }

    public CommissioningParameters openCommissioningWindow(long nodeId) throws Exception {
        return openCommissioningWindow(nodeId, 300, 1000, 1, null);
    }

    public List<CommissionableNodeData> discoverCommissionableNodes() throws Exception {
        JsonElement result = sendCommand(APICommand.DISCOVER).get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, new TypeToken<List<CommissionableNodeData>>() {}.getType());
    }

    public void removeNode(long nodeId) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        sendCommand(APICommand.REMOVE_NODE, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void interviewNode(long nodeId) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        sendCommand(APICommand.INTERVIEW_NODE, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public JsonElement sendDeviceCommand(long nodeId, int endpointId, int clusterId,
                                         String commandName, Map<String, Object> payload) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("endpoint_id", endpointId);
        args.put("cluster_id", clusterId);
        args.put("command_name", commandName);
        args.put("payload", payload != null ? payload : Map.of());
        return sendCommand(APICommand.DEVICE_COMMAND, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public Map<String, Object> readAttribute(long nodeId, String attributePath) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("attribute_path", attributePath);
        JsonElement result = sendCommand(APICommand.READ_ATTRIBUTE, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, new TypeToken<Map<String, Object>>() {}.getType());
    }

    public Map<String, Object> readAttribute(long nodeId, List<String> attributePaths) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("attribute_path", attributePaths);
        JsonElement result = sendCommand(APICommand.READ_ATTRIBUTE, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, new TypeToken<Map<String, Object>>() {}.getType());
    }

    public void writeAttribute(long nodeId, String attributePath, Object value) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("attribute_path", attributePath);
        args.put("value", value);
        sendCommand(APICommand.WRITE_ATTRIBUTE, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public Map<String, Boolean> pingNode(long nodeId) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        JsonElement result = sendCommand(APICommand.PING_NODE, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, new TypeToken<Map<String, Boolean>>() {}.getType());
    }

    public List<String> getNodeIpAddresses(long nodeId, boolean preferCache, boolean scoped) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("prefer_cache", preferCache);
        args.put("scoped", scoped);
        JsonElement result = sendCommand(APICommand.GET_NODE_IP_ADDRESSES, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, new TypeToken<List<String>>() {}.getType());
    }

    public List<String> getNodeIpAddresses(long nodeId) throws Exception {
        return getNodeIpAddresses(nodeId, true, false);
    }

    public MatterSoftwareVersion checkNodeUpdate(long nodeId) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        JsonElement result = sendCommand(APICommand.CHECK_NODE_UPDATE, args)
                .get(timeoutSeconds, TimeUnit.SECONDS);
        if (result == null || result.isJsonNull()) {
            return null;
        }
        return gson.fromJson(result, MatterSoftwareVersion.class);
    }

    public void updateNode(long nodeId, int softwareVersion) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("software_version", softwareVersion);
        sendCommand(APICommand.UPDATE_NODE, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void updateNode(long nodeId, String softwareVersion) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("node_id", MatterJson.unsigned(nodeId));
        args.put("software_version", softwareVersion);
        sendCommand(APICommand.UPDATE_NODE, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public ServerDiagnostics getDiagnostics() throws Exception {
        JsonElement result = sendCommand(APICommand.SERVER_DIAGNOSTICS).get(timeoutSeconds, TimeUnit.SECONDS);
        return gson.fromJson(result, ServerDiagnostics.class);
    }

    public void setDefaultFabricLabel(String label) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("label", label);
        sendCommand(APICommand.SET_DEFAULT_FABRIC_LABEL, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void importTestNode(String dump) throws Exception {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("dump", dump);
        sendCommand(APICommand.IMPORT_TEST_NODE, args).get(timeoutSeconds, TimeUnit.SECONDS);
    }

    // -- Low-level command sending --

    public CompletableFuture<JsonElement> sendCommand(APICommand command) {
        return sendCommand(command, null);
    }

    public CompletableFuture<JsonElement> sendCommand(APICommand command, Map<String, Object> args) {
        return sendCommand(command.getValue(), args);
    }

    public CompletableFuture<JsonElement> sendCommand(String command, Map<String, Object> args) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(new ConnectionException("Not connected"));
        }

        String messageId = String.valueOf(messageIdCounter.incrementAndGet());
        CommandMessage message = new CommandMessage(messageId, command, args);

        CompletableFuture<JsonElement> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);

        future.whenComplete((result, ex) -> pendingRequests.remove(messageId));

        try {
            connection.sendMessage(message);
        } catch (Exception e) {
            pendingRequests.remove(messageId);
            future.completeExceptionally(e);
        }

        return future;
    }

    // -- Message handling --

    private void handleIncomingMessage(JsonObject msg) {
        if (msg.has("error_code")) {
            handleErrorResult(msg);
        } else if (msg.has("result")) {
            handleSuccessResult(msg);
        } else if (msg.has("event")) {
            handleEvent(msg);
        } else {
            LOGGER.warn("Received message with unknown format: {}", msg);
        }
    }

    private void handleSuccessResult(JsonObject msg) {
        String messageId = msg.get("message_id").getAsString();
        CompletableFuture<JsonElement> future = pendingRequests.get(messageId);
        if (future != null) {
            future.complete(msg.get("result"));
        }
    }

    private void handleErrorResult(JsonObject msg) {
        String messageId = msg.get("message_id").getAsString();
        CompletableFuture<JsonElement> future = pendingRequests.get(messageId);
        if (future != null) {
            int errorCode = msg.get("error_code").getAsInt();
            String details = msg.has("details") && !msg.get("details").isJsonNull()
                    ? msg.get("details").getAsString()
                    : "Unknown error";
            future.completeExceptionally(MatterException.fromErrorCode(errorCode, details));
        }
    }

    private void handleEvent(JsonObject msg) {
        String eventStr = msg.get("event").getAsString();
        JsonElement data = msg.get("data");

        EventType eventType = EventType.fromValueOrNull(eventStr);
        if (eventType == null) {
            LOGGER.debug("Ignoring unknown event type: {}", eventStr);
            return;
        }

        // Update local node cache based on events
        switch (eventType) {
            case NODE_ADDED, NODE_UPDATED -> {
                MatterNodeData nodeData = gson.fromJson(data, MatterNodeData.class);
                nodes.put(nodeData.getNodeId(), nodeData);
            }
            case NODE_REMOVED -> {
                if (data != null && !data.isJsonNull()) {
                    nodes.remove(MatterJson.nodeId(data));
                }
            }
            case ATTRIBUTE_UPDATED -> {
                if (data != null && data.isJsonArray()) {
                    JsonArray arr = data.getAsJsonArray();
                    if (arr.size() >= 3) {
                        long nodeId = MatterJson.nodeId(arr.get(0));
                        String attrPath = arr.get(1).getAsString();
                        MatterNodeData node = nodes.get(nodeId);
                        if (node != null && node.getAttributes() != null) {
                            Object value = gson.fromJson(arr.get(2), Object.class);
                            node.getAttributes().put(attrPath, value);
                        }
                    }
                }
            }
            case SERVER_INFO_UPDATED -> {
                // Server info is managed by the connection layer
            }
            default -> {
                // Other events are just forwarded to listeners
            }
        }

        // Notify listeners
        for (MatterEventListener listener : eventListeners) {
            try {
                listener.onEvent(eventType, data);
            } catch (Exception e) {
                LOGGER.warn("Event listener threw exception", e);
            }
        }
    }

    private void handleDisconnect() {
        for (CompletableFuture<JsonElement> future : pendingRequests.values()) {
            future.completeExceptionally(new ConnectionException("Connection lost"));
        }
        pendingRequests.clear();
    }

    @Override
    public void close() {
        disconnect();
    }
}
