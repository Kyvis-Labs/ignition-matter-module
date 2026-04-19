package com.kyvislabs.matter.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kyvislabs.matter.client.exception.MatterException;
import com.kyvislabs.matter.client.exception.NodeNotExistsException;
import com.kyvislabs.matter.client.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatterClientTest {

    private final Gson gson = new Gson();

    @Test
    void testAPICommandValues() {
        assertEquals("start_listening", APICommand.START_LISTENING.getValue());
        assertEquals("commission_with_code", APICommand.COMMISSION_WITH_CODE.getValue());
        assertEquals("device_command", APICommand.DEVICE_COMMAND.getValue());
        assertEquals("read_attribute", APICommand.READ_ATTRIBUTE.getValue());
    }

    @Test
    void testEventTypeFromValue() {
        assertEquals(EventType.NODE_ADDED, EventType.fromValue("node_added"));
        assertEquals(EventType.ATTRIBUTE_UPDATED, EventType.fromValue("attribute_updated"));
        assertEquals(EventType.SERVER_SHUTDOWN, EventType.fromValue("server_shutdown"));
        assertThrows(IllegalArgumentException.class, () -> EventType.fromValue("unknown"));
    }

    @Test
    void testCommandMessageSerialization() {
        CommandMessage msg = new CommandMessage("1", "get_nodes");
        String json = gson.toJson(msg);
        assertTrue(json.contains("\"message_id\":\"1\""));
        assertTrue(json.contains("\"command\":\"get_nodes\""));
    }

    @Test
    void testCommandMessageWithArgs() {
        CommandMessage msg = new CommandMessage("2", "commission_with_code",
                java.util.Map.of("code", "MT:Y.ABC123", "network_only", false));
        String json = gson.toJson(msg);
        assertTrue(json.contains("\"code\":\"MT:Y.ABC123\""));
    }

    @Test
    void testServerInfoDeserialization() {
        String json = """
                {
                    "fabric_id": 1,
                    "compressed_fabric_id": 12345,
                    "schema_version": 11,
                    "min_supported_schema_version": 9,
                    "sdk_version": "1.4.0",
                    "wifi_credentials_set": true,
                    "thread_credentials_set": false,
                    "bluetooth_enabled": true
                }
                """;
        ServerInfoMessage info = gson.fromJson(json, ServerInfoMessage.class);
        assertEquals(1, info.getFabricId());
        assertEquals(12345, info.getCompressedFabricId());
        assertEquals(11, info.getSchemaVersion());
        assertEquals(9, info.getMinSupportedSchemaVersion());
        assertEquals("1.4.0", info.getSdkVersion());
        assertTrue(info.isWifiCredentialsSet());
        assertFalse(info.isThreadCredentialsSet());
        assertTrue(info.isBluetoothEnabled());
    }

    @Test
    void testMatterNodeDataDeserialization() {
        String json = """
                {
                    "node_id": 1,
                    "date_commissioned": "2024-01-15T10:30:00",
                    "last_interview": "2024-01-15T10:30:00",
                    "interview_version": 6,
                    "available": true,
                    "is_bridge": false,
                    "attributes": {
                        "0/40/0": "Test Vendor",
                        "0/40/1": 65521
                    }
                }
                """;
        MatterNodeData node = gson.fromJson(json, MatterNodeData.class);
        assertEquals(1, node.getNodeId());
        assertTrue(node.isAvailable());
        assertFalse(node.isBridge());
        assertEquals("Test Vendor", node.getAttribute("0/40/0"));
        assertEquals(2, node.getAttributes().size());
    }

    @Test
    void testSuccessResultDeserialization() {
        String json = """
                {
                    "message_id": "42",
                    "result": [{"node_id": 1}, {"node_id": 2}]
                }
                """;
        SuccessResultMessage msg = gson.fromJson(json, SuccessResultMessage.class);
        assertEquals("42", msg.getMessageId());
        assertTrue(msg.getResult().isJsonArray());
        assertEquals(2, msg.getResult().getAsJsonArray().size());
    }

    @Test
    void testErrorResultDeserialization() {
        String json = """
                {
                    "message_id": "42",
                    "error_code": 5,
                    "details": "Node 99 does not exist"
                }
                """;
        ErrorResultMessage msg = gson.fromJson(json, ErrorResultMessage.class);
        assertEquals("42", msg.getMessageId());
        assertEquals(5, msg.getErrorCode());
        assertEquals("Node 99 does not exist", msg.getDetails());
    }

    @Test
    void testEventMessageDeserialization() {
        String json = """
                {
                    "event": "attribute_updated",
                    "data": [1, "1/6/0", true]
                }
                """;
        EventMessage msg = gson.fromJson(json, EventMessage.class);
        assertEquals("attribute_updated", msg.getEvent());
        assertEquals(EventType.ATTRIBUTE_UPDATED, msg.getEventType());
        assertTrue(msg.getData().isJsonArray());
    }

    @Test
    void testCommissioningParametersDeserialization() {
        String json = """
                {
                    "setup_pin_code": 12345678,
                    "setup_manual_code": "35325335079",
                    "setup_qr_code": "MT:Y.K90SO500000000000"
                }
                """;
        CommissioningParameters params = gson.fromJson(json, CommissioningParameters.class);
        assertEquals(12345678, params.getSetupPinCode());
        assertEquals("35325335079", params.getSetupManualCode());
        assertEquals("MT:Y.K90SO500000000000", params.getSetupQrCode());
    }

    @Test
    void testMatterExceptionFromErrorCode() {
        MatterException ex = MatterException.fromErrorCode(5, "Node not found");
        assertInstanceOf(NodeNotExistsException.class, ex);
        assertEquals(5, ex.getErrorCode());
        assertEquals("Node not found", ex.getMessage());
    }

    @Test
    void testMatterExceptionUnknownCode() {
        MatterException ex = MatterException.fromErrorCode(999, "Something weird");
        assertInstanceOf(MatterException.class, ex);
        assertEquals(999, ex.getErrorCode());
    }

    @Test
    void testClientNotConnected() {
        MatterClient client = new MatterClient("ws://localhost:5580/ws");
        assertFalse(client.isConnected());
        assertNull(client.getServerInfo());
    }

    @Test
    void testSendCommandWhenNotConnected() {
        MatterClient client = new MatterClient("ws://localhost:5580/ws");
        var future = client.sendCommand(APICommand.GET_NODES);
        assertTrue(future.isCompletedExceptionally());
    }
}
