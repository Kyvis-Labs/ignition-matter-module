# Matter Tag Provider

An [Ignition](https://inductiveautomation.com/) module that connects to a
[python-matter-server](https://github.com/home-assistant-libs/python-matter-server) over WebSocket
and exposes Matter smart home device data as a managed tag provider.

Built for Ignition **8.3.4**.

## Features

- Managed tag provider backed by a live WebSocket connection to python-matter-server
- Tag tree built from real-time Matter node data with human-readable endpoint, cluster, and
  attribute names
- Attribute write-through: writes to tags are sent back to the device
- Automatic reconnect (maintenance task runs every 10 seconds)
- Server metadata surfaced under `Server/` (`Connected`, `FabricId`, `SDKVersion`, `SchemaVersion`)

## Tag Layout

```
Nodes/
  Node {id}/
    Attributes/
      {EndpointName}/
        {ClusterName}/
          {AttributeName}
Server/
  Connected
  FabricId
  SDKVersion
  SchemaVersion
```

Numeric Matter paths (e.g. `2/1026/0`) are translated to readable paths
(e.g. `TemperatureSensor/TemperatureMeasurement/MeasuredValue`) using bidirectional maps so writes
can be mapped back to the underlying IDs.

## Project Structure

Maven multi-module project:

| Module | Description |
| --- | --- |
| `matter-client` | Standalone Java WebSocket client for python-matter-server. No Ignition dependencies. |
| `matter-tag-provider-gateway` | Gateway-scoped Ignition runtime: module hook, extension point, tag provider. |
| `matter-tag-provider-build` | Packaging only. Assembles the `.modl` via `ignition-maven-plugin`. |

### Key Classes (`com.matter.ignition.gateway`)

- `MatterTagProviderGatewayHook` — module lifecycle hook; registers the extension point.
- `MatterTagProviderExtensionPoint` — creates `MatterTagProvider` instances from user settings.
- `MatterTagProviderSettings` — configurable server URL (requires `ws://` or `wss://`).
- `MatterTagProvider` — core `GatewayTagProvider`; manages the WebSocket, tag tree, and writes.
- `MatterNames` — lookup tables mapping Matter numeric IDs to human-readable names.

## Build

```bash
# Full build — produces the .modl in matter-tag-provider-build/target/
mvn package

# Skip tests
mvn package -DskipTests
```

## Configuration

After installing the module in Ignition, add a new tag provider of type **Matter** in the Gateway
web UI and set the python-matter-server URL (`ws://host:5580/ws` or equivalent).

## Events Handled

`NODE_ADDED`, `NODE_UPDATED`, `NODE_REMOVED`, `ATTRIBUTE_UPDATED`, `SERVER_SHUTDOWN`,
`SERVER_INFO_UPDATED`.
