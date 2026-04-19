package com.kyvislabs.matter.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kyvislabs.matter.client.exception.ConnectionException;
import com.kyvislabs.matter.client.model.CommandMessage;
import com.kyvislabs.matter.client.model.ServerInfoMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MatterClientConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatterClientConnection.class);
    private static final int SCHEMA_VERSION = 11;

    private final Gson gson = new GsonBuilder().create();
    private final URI serverUri;

    private WebSocketClient wsClient;
    private ServerInfoMessage serverInfo;
    private Consumer<JsonObject> messageHandler;
    private Runnable disconnectHandler;
    private CompletableFuture<ServerInfoMessage> connectFuture;

    MatterClientConnection(URI serverUri) {
        this.serverUri = serverUri;
    }

    boolean isConnected() {
        return wsClient != null && wsClient.isOpen();
    }

    ServerInfoMessage getServerInfo() {
        return serverInfo;
    }

    CompletableFuture<ServerInfoMessage> connect(Consumer<JsonObject> messageHandler, Runnable disconnectHandler) {
        if (wsClient != null) {
            throw new ConnectionException("Already connected");
        }

        this.messageHandler = messageHandler;
        this.disconnectHandler = disconnectHandler;
        this.connectFuture = new CompletableFuture<>();

        wsClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                LOGGER.debug("WebSocket connection opened");
            }

            @Override
            public void onMessage(String message) {
                handleRawMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                LOGGER.info("WebSocket closed: code={}, reason={}", code, reason);
                if (connectFuture != null && !connectFuture.isDone()) {
                    connectFuture.completeExceptionally(
                            new ConnectionException("Connection closed during handshake: " + reason));
                }
                if (MatterClientConnection.this.disconnectHandler != null) {
                    MatterClientConnection.this.disconnectHandler.run();
                }
            }

            @Override
            public void onError(Exception ex) {
                LOGGER.warn("WebSocket error", ex);
                if (connectFuture != null && !connectFuture.isDone()) {
                    connectFuture.completeExceptionally(
                            new ConnectionException("Connection failed", ex));
                }
            }
        };

        wsClient.connect();
        return connectFuture;
    }

    void disconnect() {
        if (wsClient != null) {
            wsClient.close();
            wsClient = null;
        }
        serverInfo = null;
    }

    void sendMessage(CommandMessage message) {
        if (!isConnected()) {
            throw new ConnectionException("Not connected");
        }
        String json = gson.toJson(message);
        LOGGER.debug("Sending: {}", json);
        wsClient.send(json);
    }

    private void handleRawMessage(String raw) {
        LOGGER.debug("Received: {}", raw);
        try {
            JsonObject json = gson.fromJson(raw, JsonObject.class);

            // First message from the server is always ServerInfoMessage
            if (serverInfo == null && json.has("sdk_version")) {
                serverInfo = gson.fromJson(json, ServerInfoMessage.class);
                validateSchemaVersion(serverInfo);
                LOGGER.info("Connected to Matter Fabric {} ({}), Schema version {}, SDK Version {}",
                        serverInfo.getFabricId(),
                        serverInfo.getCompressedFabricId(),
                        serverInfo.getSchemaVersion(),
                        serverInfo.getSdkVersion());
                if (connectFuture != null) {
                    connectFuture.complete(serverInfo);
                }
                return;
            }

            if (messageHandler != null) {
                messageHandler.accept(json);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to process message: {}", raw, e);
            if (connectFuture != null && !connectFuture.isDone()) {
                connectFuture.completeExceptionally(e);
            }
        }
    }

    private void validateSchemaVersion(ServerInfoMessage info) {
        if (info.getSchemaVersion() < SCHEMA_VERSION) {
            disconnect();
            throw new ConnectionException(
                    "Matter schema version is incompatible: " + SCHEMA_VERSION +
                            ", the server supports at most " + info.getSchemaVersion() +
                            " - update the Matter server to a more recent version or downgrade the client.");
        }
        if (info.getMinSupportedSchemaVersion() > SCHEMA_VERSION) {
            disconnect();
            throw new ConnectionException(
                    "Matter schema version is incompatible: " + SCHEMA_VERSION +
                            ", the server requires at least " + info.getMinSupportedSchemaVersion() +
                            " - update the Matter client to a more recent version or downgrade the server.");
        }
    }
}
