# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ignition module that connects to a Matter server via WebSocket and exposes Matter smart home device data as a managed tag provider in Inductive Automation's Ignition platform (v8.3.9).

Targets [matterjs-server](https://github.com/matter-js/matterjs-server) (the matter.js controller, WebSocket schema 13) and the older [python-matter-server](https://github.com/matter-js/python-matter-server) (schema 11), which share a WebSocket API.

**Unsigned 64-bit IDs are the sharp edge here.** Matter node and fabric IDs are uint64 sent as unquoted JSON numbers. Gson truncates them silently in an `int` field and throws in a `long` field, so all JSON goes through `MatterJson.gson()`, node IDs are `long` bit patterns rendered with `Long.toUnsignedString`, outbound IDs go back as `MatterJson.unsigned(...)`, and fabric IDs are `BigInteger`. Never use `new Gson()` or `getAsLong()`/`getAsInt()` on an identifier.

## Build

```bash
# Full build (produces .modl file in matter-tag-provider-build/target/)
mvn package

# Build without tests
mvn package -DskipTests
```

The build uses the `ignition-maven-plugin` to package a `.modl` file — the deployable Ignition module artifact.

## Architecture

This is a Maven multi-module project with three submodules:

- **matter-client** — Standalone Java WebSocket client for the Matter server API (`com.kyvislabs.matter:java-matter-client`, package `com.kyvislabs.matter.client`). Java 17, depends only on `Java-WebSocket` and `gson`. Has no Ignition dependencies so it can be developed and tested independently.
- **matter-tag-provider-gateway** — Gateway-scoped runtime code. Contains the module hook and tag provider implementation. Java 17, depends on the Ignition SDK (`ignition-common`, `gateway-api`) and the `matter-client` submodule.
- **matter-tag-provider-build** — Packaging only. Uses `ignition-maven-plugin` to assemble the `.modl` file from the gateway module. No source code.

### Key Classes (all in `com.matter.ignition.gateway`)

- **MatterTagProviderGatewayHook** — Module lifecycle hook (`AbstractGatewayModuleHook`). Registers the extension point; the tag provider instances are created by the extension point, not directly by the hook.
- **MatterTagProviderExtensionPoint** — `TagProviderExtensionPoint` implementation. Creates `MatterTagProvider` instances from user-configured settings. Handles settings validation (requires `ws://` or `wss://` URL).
- **MatterTagProviderSettings** — Java record: server URL plus optional Wi-Fi/Thread commissioning credentials. Rendered as a form in the Gateway web UI. Credential fields must be `SecretConfig`, not `String`, or the gateway refuses the schema at startup.
- **MatterProviderRegistry** — name → running provider, so scripting can reach a live client.
- **MatterScriptModule** — `system.matter.*`, registered from the hook's `initializeScriptManager`. Gateway scope only; Vision/Designer would need `getRpcImplementation`.
- **MatterTagProvider** — Core `GatewayTagProvider` implementation. Manages the WebSocket connection, builds an in-memory tag tree from Matter node data, handles real-time event updates (node added/updated/removed, attribute changes), and supports attribute writes back to devices.
- **MatterNames** — Static lookup tables mapping Matter numeric IDs to human-readable names for device types, clusters, and attributes.

### Tag Tree Structure

Tags are organized as: `{NodeFolder}/Nodes/{EndpointName}/{ClusterName}/{AttributeName}`, where
`{NodeFolder}` comes from the node's `NodeLabel` attribute and falls back to `Node {unsigned id}`.
Each node folder also has `ShowRawData` and a `Commands/` folder; the root has `Server/` and
`Commands/`.

The provider translates numeric Matter paths (e.g., `2/1026/0`) into readable paths (e.g., `TemperatureSensor/TemperatureMeasurement/MeasuredValue`) using bidirectional maps for write support. Server metadata tags live under `Server/` (Connected, FabricId, SDKVersion, SchemaVersion).

### Connection Model

`MatterClient` connects via WebSocket. A maintenance task runs every 10 seconds to reconnect if disconnected. Events from the server (`NODE_ADDED`, `NODE_UPDATED`, `NODE_REMOVED`, `ATTRIBUTE_UPDATED`, `SERVER_SHUTDOWN`, `SERVER_INFO_UPDATED`) drive tag tree mutations. Unknown event types are logged and ignored rather than throwing — matterjs-server adds events the python server does not have.

### Local test setup

`docker run -d --name matterjs-test --network devnet -v ~/.local/share/matterjs-test/data:/data ghcr.io/matter-js/matterjs-server:stable --storage-path /data --fabricid 1`

A realistic test node with a 64-bit ID can be made by taking a real node from any server and feeding it to `import_test_node` as `{"data":{"node": <node>}}`; matterjs-server assigns it an ID at `0xFFFF_FFFE_0000_0000`.
