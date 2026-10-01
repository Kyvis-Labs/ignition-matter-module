# Matter Tag Provider

An [Ignition](https://inductiveautomation.com/) module that connects to a Matter server over
WebSocket and exposes Matter smart home device data as a managed tag provider.

Works against either server implementation:

- **[matterjs-server](https://github.com/matter-js/matterjs-server)** (preferred) — the matter.js
  controller from the Open Home Foundation, Matter 1.6, WebSocket schema 13.
- **[python-matter-server](https://github.com/matter-js/python-matter-server)** — the older
  implementation whose WebSocket API matterjs-server reimplements.

Built for Ignition **8.3.9**.

### Unsigned 64-bit identifiers

Matter node and fabric IDs are unsigned 64-bit and arrive as unquoted JSON numbers. Stock Gson
handling of those is unsafe — an `int` field truncates silently and a `long` field throws — so the
client reads them through `BigInteger` (see `MatterJson`). Node IDs are carried as `long` bit
patterns and must be rendered with `Long.toUnsignedString`; fabric IDs are `BigInteger`. This is not
theoretical: matterjs-server allocates test node IDs from `0xFFFF_FFFE_0000_0000` and randomises the
fabric ID unless `--fabricid` is given.

## Features

- Managed tag provider backed by a live WebSocket connection to python-matter-server
- Tag tree built from real-time Matter node data with human-readable endpoint, cluster, and
  attribute names
- Attribute write-through: writes to tags are sent back to the device
- Automatic reconnect (maintenance task runs every 10 seconds)
- Server metadata surfaced under `Server/` (`Connected`, `FabricId`, `SDKVersion`, `SchemaVersion`)
- Commissioning and node management from Ignition, as `system.matter.*` gateway scripting functions
  and as write-to-trigger command tags

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
| `matter-client` | Standalone Java WebSocket client for the Matter server API. No Ignition dependencies. |
| `matter-tag-provider-gateway` | Gateway-scoped Ignition runtime: module hook, extension point, tag provider. |
| `matter-tag-provider-build` | Packaging only. Assembles the `.modl` via `ignition-maven-plugin`. |

### Key Classes (`com.matter.ignition.gateway`)

- `MatterTagProviderGatewayHook` — module lifecycle hook; registers the extension point.
- `MatterTagProviderExtensionPoint` — creates `MatterTagProvider` instances from user settings.
- `MatterTagProviderSettings` — server URL (requires `ws://` or `wss://`), plus optional Wi-Fi and
  Thread commissioning credentials stored as `SecretConfig`.
- `MatterProviderRegistry` — tracks running providers by name so scripting can reach them.
- `MatterScriptModule` — the `system.matter.*` gateway scripting functions.
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

After installing the module in Ignition, add a new tag provider of type **Matter Server** in the
Gateway web UI and set the Matter server URL (`ws://host:5580/ws` or equivalent).

The module is unsigned, so the gateway needs `-Dignition.allowunsignedmodules=true` in
`data/ignition.conf` (Java Additional Parameters) and a full restart — a reload is not enough.

### Commissioning

Commissioning has no natural tag representation, so it is exposed two ways.

Gateway scripting (Perspective, gateway event scripts, tag event scripts):

```python
system.matter.getProviders()
system.matter.commissionWithCode("MatterJs", "1234-567-8901")
system.matter.openCommissioningWindow("MatterJs", "1")   # returns QR + manual code
system.matter.discover("MatterJs")
system.matter.removeNode("MatterJs", "1")                # scripting only; not a command tag
```

Node IDs cross the scripting boundary as decimal strings, because an unsigned 64-bit ID does not
fit a Java `long` and Jython would otherwise render test node IDs as negative numbers.

Command tags, written to trigger (any truthy write runs the command and the tag resets):

```
Commands/CommissionCode      (write the pairing code here first)
Commands/Commission
Commands/Discover
Commands/Refresh
Commands/LastCommand | LastResult | LastQRCode | LastManualCode | LastError
<node folder>/Commands/OpenCommissioningWindow | Interview | Ping | CheckUpdate
```

### Thread devices

matterjs-server cannot commission Thread devices directly yet — its README notes matter.js needs a
network name it is not given. Pair the device in another ecosystem first, then use that app's
"share device" flow and feed the resulting code to `commissionWithCode`.

## Events Handled

`NODE_ADDED`, `NODE_UPDATED`, `NODE_REMOVED`, `ATTRIBUTE_UPDATED`, `SERVER_SHUTDOWN`,
`SERVER_INFO_UPDATED`.
