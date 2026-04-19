# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ignition module that connects to a [python-matter-server](https://github.com/home-assistant-libs/python-matter-server) via WebSocket and exposes Matter smart home device data as a managed tag provider in Inductive Automation's Ignition platform (v8.3.4).

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

- **matter-client** — Standalone Java WebSocket client for python-matter-server (`com.kyvislabs.matter:java-matter-client`, package `com.kyvislabs.matter.client`). Java 17, depends only on `Java-WebSocket` and `gson`. Has no Ignition dependencies so it can be developed and tested independently.
- **matter-tag-provider-gateway** — Gateway-scoped runtime code. Contains the module hook and tag provider implementation. Java 17, depends on the Ignition SDK (`ignition-common`, `gateway-api`) and the `matter-client` submodule.
- **matter-tag-provider-build** — Packaging only. Uses `ignition-maven-plugin` to assemble the `.modl` file from the gateway module. No source code.

### Key Classes (all in `com.matter.ignition.gateway`)

- **MatterTagProviderGatewayHook** — Module lifecycle hook (`AbstractGatewayModuleHook`). Registers the extension point; the tag provider instances are created by the extension point, not directly by the hook.
- **MatterTagProviderExtensionPoint** — `TagProviderExtensionPoint` implementation. Creates `MatterTagProvider` instances from user-configured settings. Handles settings validation (requires `ws://` or `wss://` URL).
- **MatterTagProviderSettings** — Java record defining the configurable server URL. Rendered as a form in the Ignition Gateway web UI.
- **MatterTagProvider** — Core `GatewayTagProvider` implementation. Manages WebSocket connection to python-matter-server, builds an in-memory tag tree from Matter node data, handles real-time event updates (node added/updated/removed, attribute changes), and supports attribute writes back to devices.
- **MatterNames** — Static lookup tables mapping Matter numeric IDs to human-readable names for device types, clusters, and attributes.

### Tag Tree Structure

Tags are organized as: `Nodes/Node {id}/Attributes/{EndpointName}/{ClusterName}/{AttributeName}`

The provider translates numeric Matter paths (e.g., `2/1026/0`) into readable paths (e.g., `TemperatureSensor/TemperatureMeasurement/MeasuredValue`) using bidirectional maps for write support. Server metadata tags live under `Server/` (Connected, FabricId, SDKVersion, SchemaVersion).

### Connection Model

`MatterClient` connects via WebSocket. A maintenance task runs every 10 seconds to reconnect if disconnected. Events from the server (`NODE_ADDED`, `NODE_UPDATED`, `NODE_REMOVED`, `ATTRIBUTE_UPDATED`, `SERVER_SHUTDOWN`, `SERVER_INFO_UPDATED`) drive tag tree mutations.
