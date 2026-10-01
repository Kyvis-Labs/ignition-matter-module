package com.matter.ignition.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.inductiveautomation.ignition.common.browsing.BrowseFilter;
import com.inductiveautomation.ignition.common.browsing.Results;
import com.inductiveautomation.ignition.common.config.ConfigurationPropertyModel;
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.opc.BrowseElement;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.tags.browsing.NodeDescription;
import com.inductiveautomation.ignition.common.tags.config.CollisionPolicy;
import com.inductiveautomation.ignition.common.tags.config.EditRights;
import com.inductiveautomation.ignition.common.tags.config.BasicTagConfigurationModel;
import com.inductiveautomation.ignition.common.tags.config.TagConfiguration;
import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel;
import com.inductiveautomation.ignition.common.tags.config.TagGroupConfiguration;
import com.inductiveautomation.ignition.common.tags.config.properties.WellKnownTagProps;
import com.inductiveautomation.ignition.common.tags.config.model.TagReference;
import com.inductiveautomation.ignition.common.tags.config.model.TagReferenceQuery;
import com.inductiveautomation.ignition.common.tags.config.types.TagObjectType;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.NamedValue;
import com.inductiveautomation.ignition.common.tags.model.TagProviderInformation;
import com.inductiveautomation.ignition.common.tags.model.TagProviderProps;
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent;
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener;
import com.inductiveautomation.ignition.common.tags.query.TagQueryFilter;
import com.inductiveautomation.ignition.common.tags.status.TagDiagnostics;
import com.inductiveautomation.ignition.gateway.historian.TagHistoryQueryInterface;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.model.ProfileStatus;
import com.inductiveautomation.ignition.gateway.secrets.Plaintext;
import com.inductiveautomation.ignition.gateway.secrets.Secret;
import com.inductiveautomation.ignition.gateway.secrets.SecretConfig;
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagProvider;
import com.inductiveautomation.ignition.gateway.tags.model.TagStructureListener;
import com.inductiveautomation.ignition.gateway.tags.model.TagSubscription;
import com.inductiveautomation.ignition.gateway.tags.model.TagSubscriptionChangeEvent;
import com.inductiveautomation.ignition.gateway.tags.model.TagSubscriptionChangeListener;
import com.inductiveautomation.ignition.gateway.tags.model.TagSubscriptionModel;
import com.kyvislabs.matter.client.MatterClient;
import com.kyvislabs.matter.client.MatterJson;
import com.kyvislabs.matter.client.model.EventType;
import com.kyvislabs.matter.client.model.MatterNodeData;
import com.kyvislabs.matter.client.model.ServerInfoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MatterTagProvider implements GatewayTagProvider {

    private static final String MODULE_ID = MatterTagProviderGatewayHook.MODULE_ID;

    /** Folder holding write-to-trigger command tags, at the root and under each node. */
    static final String CMD = "Commands";

    private final GatewayContext context;
    private final String name;
    private final String serverUrl;
    private final MatterTagProviderSettings settings;
    private final Logger logger;
    private final Gson gson = MatterJson.gson();

    private final ConcurrentHashMap<String, TagNode> tags = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> childIndex = new ConcurrentHashMap<>();
    private final List<TagStructureListener> structureListeners = new CopyOnWriteArrayList<>();

    // numeric attrPath -> readable path segment per node (e.g., "2/1026/0" -> "TemperatureSensor/TemperatureMeasurement/MeasuredValue")
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, String>> numericToReadable = new ConcurrentHashMap<>();
    // reverse of above for write support
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, String>> readableToNumeric = new ConcurrentHashMap<>();
    // endpoint ID -> readable name per node (e.g., "2" -> "TemperatureSensor")
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, String>> endpointNameCache = new ConcurrentHashMap<>();
    // endpoint ID -> device type ID per node (for infrastructure filtering)
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, Integer>> endpointDeviceTypeIdCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, String> nodeFolderNames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> folderNameToNodeId = new ConcurrentHashMap<>();

    // Raw data toggle state per node
    private final ConcurrentHashMap<Long, Boolean> rawDataEnabled = new ConcurrentHashMap<>();
    // Cached attribute maps per node for building raw data on demand
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> nodeAttributeCache = new ConcurrentHashMap<>();

    private final TagSubscriptionChangeListener subscriptionChangeListener = this::onSubscriptionChanged;
    private TagSubscriptionModel subscriptionModel;
    private volatile MatterClient matterClient;
    private volatile boolean running = false;

    private final AtomicLong eventsReceived = new AtomicLong(0);
    private final AtomicLong attributeWritesSent = new AtomicLong(0);
    private final AtomicLong connectionAttempts = new AtomicLong(0);

    /** Pairing code staged by a write to {@code Commands/CommissionCode}. */
    private volatile String commissionCode = "";

    private static class TagNode {
        final DataType dataType;
        final boolean isFolder;
        volatile QualifiedValue currentValue;

        TagNode(DataType dataType, boolean isFolder, QualifiedValue value) {
            this.dataType = dataType;
            this.isFolder = isFolder;
            this.currentValue = value;
        }
    }

    MatterTagProvider(GatewayContext context, String name, MatterTagProviderSettings settings) {
        this.context = context;
        this.name = name;
        this.serverUrl = settings.serverUrl();
        this.settings = settings;
        this.logger = LoggerFactory.getLogger(getClass().getName() + "." + name);
    }

    /**
     * The live client, for the scripting layer. Commands are rejected rather than queued when the
     * server is unreachable, so callers get an immediate, obvious failure.
     */
    MatterClient requireClient() {
        MatterClient client = matterClient;
        if (client == null || !client.isConnected()) {
            throw new IllegalStateException(
                    "Matter tag provider '" + name + "' is not connected to " + serverUrl);
        }
        return client;
    }

    // ---- GatewayTagProvider lifecycle ----

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setup(TagSubscriptionModel model, boolean b) {
        this.subscriptionModel = model;
        model.addListener(name, subscriptionChangeListener);
    }

    @Override
    public void startup() {
        running = true;
        MatterProviderRegistry.register(name, this);

        putTag("Server/Connected", DataType.Boolean, false);
        putTag("Server/FabricId", DataType.String, "");
        putTag("Server/SDKVersion", DataType.String, "");
        putTag("Server/SchemaVersion", DataType.Int4, 0);
        putTag("Server/URL", DataType.String, serverUrl);
        putTag("Server/LastConnectedTime", DataType.String, "");
        putTag("Server/LastDisconnectedTime", DataType.String, "");
        putTag("Server/ConnectionAttempts", DataType.Int8, 0L);
        putTag("Server/EventsReceived", DataType.Int8, 0L);
        putTag("Server/AttributeWritesSent", DataType.Int8, 0L);
        putTag("Server/LastEventTime", DataType.String, "");
        putTag("Server/NodeCount", DataType.Int4, 0);
        putTag("Server/LastError", DataType.String, "");

        putFolder(CMD);
        putTag(CMD + "/CommissionCode", DataType.String, "");
        putTag(CMD + "/Commission", DataType.Boolean, false);
        putTag(CMD + "/Discover", DataType.Boolean, false);
        putTag(CMD + "/Refresh", DataType.Boolean, false);
        putTag(CMD + "/LastCommand", DataType.String, "");
        putTag(CMD + "/LastResult", DataType.String, "");
        putTag(CMD + "/LastQRCode", DataType.String, "");
        putTag(CMD + "/LastManualCode", DataType.String, "");
        putTag(CMD + "/LastError", DataType.String, "");

        context.getExecutionManager().register(
                MODULE_ID, "MatterMaintain-" + name, this::maintainConnection, 10_000);

        connect();
    }

    @Override
    public void shutdown() {
        running = false;
        MatterProviderRegistry.unregister(name);

        if (subscriptionModel != null) {
            subscriptionModel.removeListener(name, subscriptionChangeListener);
        }

        try {
            context.getExecutionManager().unRegister(MODULE_ID, "MatterMaintain-" + name);
        } catch (Exception e) {
            logger.warn("Error unregistering maintenance task.", e);
        }

        disconnect();
        tags.clear();
        childIndex.clear();
        numericToReadable.clear();
        readableToNumeric.clear();
        endpointNameCache.clear();
        endpointDeviceTypeIdCache.clear();
        nodeFolderNames.clear();
        folderNameToNodeId.clear();
        rawDataEnabled.clear();
        nodeAttributeCache.clear();
    }

    @Override
    public void addStructureListener(TagStructureListener listener) {
        structureListeners.add(listener);
    }

    @Override
    public void removeStructureListener(TagStructureListener listener) {
        structureListeners.remove(listener);
    }

    // ---- Browse ----

    @Override
    public CompletableFuture<Results<NodeDescription>> browseAsync(
            TagPath parent, BrowseFilter filter, SecurityContext securityContext) {
        String parentStr = tagPathToString(parent);
        Set<String> kids = childIndex.getOrDefault(parentStr, Set.of());

        List<NodeDescription> results = new ArrayList<>();
        for (String childName : kids) {
            String childPath = parentStr.isEmpty() ? childName : parentStr + "/" + childName;
            TagNode node = tags.get(childPath);
            if (node == null) continue;

            var builder = NodeDescription.newBuilder()
                    .name(childName)
                    .hasChildren(node.isFolder);

            if (node.isFolder) {
                builder.objectType(TagObjectType.Folder);
            } else {
                builder.objectType(TagObjectType.AtomicTag)
                        .dataType(node.dataType)
                        .value(node.currentValue);
            }

            results.add(builder.build());
        }

        return CompletableFuture.completedFuture(Results.of(results));
    }

    // ---- Read ----

    @Override
    public CompletableFuture<List<QualifiedValue>> readAsync(
            List<TagPath> paths, SecurityContext securityContext) {
        List<QualifiedValue> values = new ArrayList<>(paths.size());
        for (TagPath path : paths) {
            TagNode node = tags.get(tagPathToString(path));
            if (node != null) {
                values.add(node.currentValue);
            } else {
                values.add(new BasicQualifiedValue(null, QualityCode.Bad));
            }
        }
        return CompletableFuture.completedFuture(values);
    }

    // ---- Write ----

    @Override
    public CompletableFuture<List<QualityCode>> writeAsync(
            List<TagPath> paths, List<QualifiedValue> values, SecurityContext securityContext) {
        List<QualityCode> results = new ArrayList<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            try {
                handleAttributeWrite(tagPathToString(paths.get(i)), values.get(i).getValue());
                results.add(QualityCode.Good);
            } catch (Exception e) {
                logger.error("Write error: {}", paths.get(i), e);
                results.add(QualityCode.Bad);
            }
        }
        return CompletableFuture.completedFuture(results);
    }

    // ---- Status & Properties ----

    @Override
    public CompletableFuture<TagProviderInformation> getStatusInformation() {
        boolean connected = matterClient != null && matterClient.isConnected();
        String status = connected
                ? ProfileStatus.RUNNING.getMessage().toString()
                : "Disconnected from " + serverUrl;

        TagProviderInformation info = new TagProviderInformation(name, status, true);
        // The gateway's provider list reads the tag count out of the "tags" property; without it
        // the column just says "Not Available".
        long tagCount = tags.values().stream().filter(node -> !node.isFolder).count();
        info.setProperties(Map.of("tags", NamedValue.of("tags", (int) tagCount)));
        return CompletableFuture.completedFuture(info);
    }

    @Override
    public CompletableFuture<TagProviderProps> getPropertiesAsync() {
        return CompletableFuture.completedFuture(
                TagProviderProps.newBuilder()
                        .name(name)
                        .editRights(EditRights.noRights())
                        .build()
        );
    }

    // ---- Stubs (unsupported operations) ----

    @Override
    public CompletableFuture<List<QualityCode>> moveTagsAsync(
            List<TagPath> tags, TagPath dest, CollisionPolicy policy, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(
                tags.stream().map(t -> QualityCode.Bad).toList());
    }

    @Override
    public CompletableFuture<Results<NodeDescription>> queryAsync(
            TagQueryFilter filter, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(Results.of(List.of()));
    }

    @Override
    public CompletableFuture<List<TagReference>> getTagReferences(TagReferenceQuery query) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<QualityCode> deleteTagReferences(TagPath path, Long start, Long end) {
        return CompletableFuture.completedFuture(QualityCode.Good);
    }

    @Override
    public CompletableFuture<QualityCode> deleteAllTagReferences() {
        return CompletableFuture.completedFuture(QualityCode.Good);
    }

    @Override
    public Optional<TagHistoryQueryInterface> getHistoryQueryInterface() {
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Results<BrowseElement>> browseTagDataSourcesAsync(
            BrowseElement parent, BrowseFilter filter) {
        return CompletableFuture.completedFuture(Results.of(List.of()));
    }

    @Override
    public CompletableFuture<List<TagConfigurationModel>> getTagConfigsAsync(
            List<TagPath> paths, boolean recursive, boolean localOnly) {
        List<TagConfigurationModel> results = new ArrayList<>(paths.size());
        for (TagPath path : paths) {
            BasicTagConfigurationModel model = buildTagConfig(path, recursive);
            if (model != null) {
                results.add(model);
            }
        }
        return CompletableFuture.completedFuture(results);
    }

    /**
     * Builds the configuration model for {@code path}, nesting the whole subtree beneath it when
     * recursive. Returns null for a path this provider does not have.
     *
     * <p>Two details the gateway's tag export depends on: the returned models must carry their
     * children via {@link BasicTagConfigurationModel#addChild} rather than being returned as
     * siblings, and a null must never be put in the result list — the exporter dereferences every
     * element, so one null fails the whole export.
     *
     * <p>The provider root is a special case: it has no {@link TagNode} of its own, existing only
     * in the child index, yet a recursive export starts there.
     */
    private BasicTagConfigurationModel buildTagConfig(TagPath path, boolean recursive) {
        String key = tagPathToString(path);
        TagNode node = tags.get(key);
        boolean isRoot = key.isEmpty();
        if (node == null && !isRoot) {
            return null;
        }

        boolean folder = isRoot || node.isFolder;
        BasicTagConfigurationModel model = BasicTagConfigurationModel.newTag(path);
        model.setType(folder ? TagObjectType.Folder : TagObjectType.AtomicTag);
        if (!folder) {
            model.set(WellKnownTagProps.DataType, node.dataType);
            model.set(WellKnownTagProps.Value, node.currentValue);
        }

        if (recursive && folder) {
            // Sorted so an export is stable between runs.
            for (String childName : new TreeSet<>(childIndex.getOrDefault(key, Set.of()))) {
                BasicTagConfigurationModel child = buildTagConfig(path.getChildPath(childName), recursive);
                if (child != null) {
                    model.addChild(child);
                }
            }
        }
        return model;
    }

    @Override
    public CompletableFuture<List<QualityCode>> saveTagConfigsAsync(
            List<TagConfiguration> configs, CollisionPolicy policy, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(
                configs.stream().map(c -> QualityCode.Bad).toList());
    }

    @Override
    public CompletableFuture<List<QualityCode>> removeTagConfigsAsync(
            List<TagPath> paths, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(
                paths.stream().map(p -> QualityCode.Bad).toList());
    }

    @Override
    public CompletableFuture<List<TagGroupConfiguration>> getTagGroupsAsync() {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<List<QualityCode>> saveTagGroupsAsync(
            List<TagGroupConfiguration> groups, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(
                groups.stream().map(g -> QualityCode.Bad).toList());
    }

    @Override
    public CompletableFuture<List<QualityCode>> removeTagGroupsAsync(
            List<String> names, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(
                names.stream().map(n -> QualityCode.Bad).toList());
    }

    @Override
    public CompletableFuture<ConfigurationPropertyModel> getTagGroupConfigModelAsync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ConfigurationPropertyModel> getTagConfigModelAsync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<TagDiagnostics> getDiagnosticsAsync(TagPath path) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<QualityCode>> importTagsAsync(
            TagPath root, String json, String importPolicy,
            CollisionPolicy collisionPolicy, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(List.of(QualityCode.Bad));
    }

    @Override
    public CompletableFuture<List<QualityCode>> importTagsZippedAsync(
            TagPath root, byte[] data, String importPolicy,
            CollisionPolicy collisionPolicy, SecurityContext secCtx) {
        return CompletableFuture.completedFuture(List.of(QualityCode.Bad));
    }

    @Override
    public void requestTagGroupExecution(String groupName) {
        // no-op
    }

    // ---- Connection management ----

    private synchronized void connect() {
        if (matterClient != null && matterClient.isConnected()) {
            return;
        }

        if (matterClient != null) {
            try {
                matterClient.close();
            } catch (Exception e) {
                logger.debug("Error closing previous client.", e);
            }
            matterClient = null;
        }

        connectionAttempts.incrementAndGet();
        updateTagValue("Server/ConnectionAttempts", connectionAttempts.get());
        logger.info("Connecting to Matter server at {}", serverUrl);

        try {
            matterClient = new MatterClient(serverUrl);
            matterClient.setTimeoutSeconds(30);

            ServerInfoMessage serverInfo = matterClient.connect();
            logger.info("Connected to Matter server '{}': {}", name, serverInfo);

            updateTagValue("Server/Connected", true);
            updateTagValue("Server/FabricId", String.valueOf(serverInfo.getFabricId()));
            updateTagValue("Server/SDKVersion", serverInfo.getSdkVersion());
            updateTagValue("Server/SchemaVersion", serverInfo.getSchemaVersion());
            updateTagValue("Server/LastConnectedTime", Instant.now().toString());
            updateTagValue("Server/LastError", "");

            pushCommissioningCredentials();

            matterClient.addEventListener(this::onMatterEvent);

            var nodes = matterClient.startListening();
            logger.info("Received {} nodes from '{}'.", nodes.size(), name);

            for (MatterNodeData node : nodes) {
                buildNodeTags(node);
            }
        } catch (Exception e) {
            logger.warn("Failed to connect to '{}' at {}: {}", name, serverUrl, e.getMessage());
            updateTagValue("Server/Connected", false);
            updateTagValue("Server/LastError", e.getMessage() != null ? e.getMessage() : e.toString());
            if (matterClient != null) {
                try {
                    matterClient.close();
                } catch (Exception ex) {
                    // ignore
                }
                matterClient = null;
            }
        }
    }

    /**
     * Hands the server any configured network credentials so BLE commissioning can join new
     * devices to the right network. Both are optional, and a failure here must not fail the
     * connection — monitoring still works without them.
     */
    private void pushCommissioningCredentials() {
        if (settings.hasWifiSsid()) {
            String password = revealSecret(settings.wifiPassword(), "Wi-Fi password");
            if (password != null) {
                try {
                    matterClient.setWifiCredentials(settings.wifiSsid(), password);
                    logger.info("Set Wi-Fi commissioning credentials for SSID '{}' on '{}'.",
                            settings.wifiSsid(), name);
                } catch (Exception e) {
                    logger.warn("Could not set Wi-Fi commissioning credentials on '{}': {}",
                            name, e.getMessage());
                }
            }
        }

        String dataset = revealSecret(settings.threadDataset(), "Thread dataset");
        if (dataset != null) {
            try {
                matterClient.setThreadDataset(dataset);
                logger.info("Set Thread commissioning dataset on '{}'.", name);
            } catch (Exception e) {
                logger.warn("Could not set Thread commissioning dataset on '{}': {}",
                        name, e.getMessage());
            }
        }
    }

    /**
     * Resolves a configured secret, whether it is stored inline or as a reference to a secret
     * provider. Returns null when unset or unreadable; credentials are optional, so a failure here
     * must not stop monitoring. The plaintext is cleared as soon as it has been handed over.
     */
    private String revealSecret(SecretConfig config, String what) {
        if (config == null) {
            return null;
        }
        try (Plaintext plaintext = Secret.create(context, config).getPlaintext()) {
            String value = plaintext.getAsString();
            return value == null || value.isBlank() ? null : value;
        } catch (Exception e) {
            logger.warn("Could not read the configured {} for '{}': {}", what, name, e.getMessage());
            return null;
        }
    }

    private synchronized void disconnect() {
        if (matterClient != null) {
            try {
                matterClient.close();
            } catch (Exception e) {
                logger.warn("Error closing Matter client for '{}'.", name, e);
            }
            matterClient = null;
        }
        updateTagValue("Server/Connected", false);
        updateTagValue("Server/LastDisconnectedTime", Instant.now().toString());
    }

    private void maintainConnection() {
        if (!running) return;
        if (matterClient == null || !matterClient.isConnected()) {
            connect();
        }
    }

    // ---- Event handling ----

    private void onMatterEvent(EventType eventType, JsonElement data) {
        try {
            eventsReceived.incrementAndGet();
            updateTagValue("Server/EventsReceived", eventsReceived.get());
            updateTagValue("Server/LastEventTime", Instant.now().toString());
            switch (eventType) {
                case NODE_ADDED, NODE_UPDATED -> {
                    MatterNodeData node = gson.fromJson(data, MatterNodeData.class);
                    buildNodeTags(node);
                }
                case NODE_REMOVED -> {
                    if (data != null && !data.isJsonNull()) {
                        removeNodeTags(MatterJson.nodeId(data));
                    }
                }
                case ATTRIBUTE_UPDATED -> {
                    if (data != null && data.isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray();
                        if (arr.size() >= 3) {
                            long nodeId = MatterJson.nodeId(arr.get(0));
                            String attrPath = arr.get(1).getAsString();
                            Object value = gson.fromJson(arr.get(2), Object.class);
                            configureAndUpdateAttributeTag(nodeId, attrPath, value);
                        }
                    }
                }
                case SERVER_SHUTDOWN -> {
                    logger.info("Matter server '{}' is shutting down.", name);
                    updateTagValue("Server/Connected", false);
                    updateTagValue("Server/LastDisconnectedTime", Instant.now().toString());
                }
                case SERVER_INFO_UPDATED -> {
                    if (matterClient != null) {
                        ServerInfoMessage info = matterClient.getServerInfo();
                        if (info != null) {
                            updateTagValue("Server/FabricId", String.valueOf(info.getFabricId()));
                            updateTagValue("Server/SDKVersion", info.getSdkVersion());
                            updateTagValue("Server/SchemaVersion", info.getSchemaVersion());
                        }
                    }
                }
                default -> { }
            }
        } catch (Exception e) {
            logger.error("Error handling Matter event {} for '{}'.", eventType, name, e);
        }
    }

    // ---- Tag tree building ----

    private void buildNodeTags(MatterNodeData node) {
        long nodeId = node.getNodeId();
        Map<String, Object> attributes = node.getAttributes() != null ? node.getAttributes() : Map.of();

        String newFolderName = computeNodeFolderName(nodeId, attributes);
        String oldFolderName = nodeFolderNames.get(nodeId);

        // Preserve raw data toggle state across rebuilds
        boolean wasRawDataEnabled = rawDataEnabled.getOrDefault(nodeId, false);

        // Clear stale tags if node already existed (NODE_UPDATED)
        if (oldFolderName != null) {
            removeNodeTags(nodeId);
        }

        nodeFolderNames.put(nodeId, newFolderName);
        folderNameToNodeId.put(newFolderName, nodeId);

        String prefix = nodePrefix(nodeId);

        putTag(prefix + "/Available", DataType.Boolean, node.isAvailable());
        putTag(prefix + "/ShowRawData", DataType.Boolean, wasRawDataEnabled);

        putFolder(prefix + "/" + CMD);
        putTag(prefix + "/" + CMD + "/OpenCommissioningWindow", DataType.Boolean, false);
        putTag(prefix + "/" + CMD + "/Interview", DataType.Boolean, false);
        putTag(prefix + "/" + CMD + "/Ping", DataType.Boolean, false);
        putTag(prefix + "/" + CMD + "/CheckUpdate", DataType.Boolean, false);
        rawDataEnabled.put(nodeId, wasRawDataEnabled);

        // Cache attributes for raw data (ConcurrentHashMap doesn't allow null values)
        ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                cacheMap.put(entry.getKey(), entry.getValue());
            }
        }
        nodeAttributeCache.put(nodeId, cacheMap);

        if (attributes.isEmpty()) {
            updateTagValue("Server/NodeCount", nodeFolderNames.size());
            return;
        }

        // Pass 1: Resolve endpoint names and device type IDs from Descriptor cluster DeviceTypeList (attr 29/0)
        TreeMap<Integer, String> epNames = new TreeMap<>();
        TreeMap<Integer, Integer> epDeviceTypeIds = new TreeMap<>();

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String[] parts = entry.getKey().split("/");
            if (parts.length != 3) continue;
            int ep = Integer.parseInt(parts[0]);
            int cluster = Integer.parseInt(parts[1]);
            int attr = Integer.parseInt(parts[2]);
            if (cluster == 29 && attr == 0) {
                String dtName = MatterNames.resolveEndpointDeviceType(entry.getValue());
                if (dtName != null) {
                    epNames.put(ep, dtName);
                }
                Integer dtId = MatterNames.resolveEndpointDeviceTypeId(entry.getValue());
                if (dtId != null) {
                    epDeviceTypeIds.put(ep, dtId);
                }
            }
        }

        // Override endpoint names with user-assigned NodeLabel where available
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String[] nlParts = entry.getKey().split("/");
            if (nlParts.length != 3) continue;
            int nlEp, nlCluster, nlAttr;
            try {
                nlEp = Integer.parseInt(nlParts[0]);
                nlCluster = Integer.parseInt(nlParts[1]);
                nlAttr = Integer.parseInt(nlParts[2]);
            } catch (NumberFormatException e) { continue; }
            if ((nlCluster == 40 || nlCluster == 57) && nlAttr == 5
                    && entry.getValue() instanceof String s && !s.isBlank()) {
                String sanitized = s.replace("/", "_").strip();
                if (!sanitized.isEmpty() && epNames.containsKey(nlEp)) {
                    epNames.put(nlEp, sanitized);
                }
            }
        }

        // Disambiguate duplicate device type names
        ConcurrentHashMap<String, String> epNameMap = new ConcurrentHashMap<>();
        HashMap<String, Integer> usedNames = new HashMap<>();
        for (Map.Entry<Integer, String> entry : epNames.entrySet()) {
            String baseName = entry.getValue();
            int count = usedNames.merge(baseName, 1, Integer::sum);
            String finalName = count == 1 ? baseName : baseName + "_" + count;
            epNameMap.put(String.valueOf(entry.getKey()), finalName);
        }
        endpointNameCache.put(nodeId, epNameMap);

        // Cache device type IDs per endpoint
        ConcurrentHashMap<String, Integer> epDtIdMap = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Integer> entry : epDeviceTypeIds.entrySet()) {
            epDtIdMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        endpointDeviceTypeIdCache.put(nodeId, epDtIdMap);

        // Init bidirectional maps for this node
        ConcurrentHashMap<String, String> n2r = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> r2n = new ConcurrentHashMap<>();
        numericToReadable.put(nodeId, n2r);
        readableToNumeric.put(nodeId, r2n);

        // Pass 2: Build tags with filtering
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String numericPath = entry.getKey();
            String[] parts = numericPath.split("/");
            if (parts.length != 3) continue;

            String epId = parts[0];
            int clusterId = Integer.parseInt(parts[1]);
            int attrId = Integer.parseInt(parts[2]);

            String epName = epNameMap.getOrDefault(epId, "Endpoint_" + epId);
            String clusterName = MatterNames.clusterName(clusterId);
            String attrName = MatterNames.attributeName(clusterId, attrId);

            String readablePath = epName + "/" + clusterName + "/" + attrName;
            n2r.put(numericPath, readablePath);
            r2n.put(readablePath, numericPath);

            DataType dataType = inferDataType(entry.getValue());
            Object tagValue = convertValue(entry.getValue());

            // Determine placement prefix based on endpoint type
            Integer deviceTypeId = epDtIdMap.get(epId);
            boolean isInfra = deviceTypeId != null && MatterNames.isInfrastructureDeviceType(deviceTypeId);
            String placementPrefix = isInfra ? prefix : prefix + "/Nodes/" + epName;

            // 1. Promoted attribute (e.g., Reachable at device root)
            String promotedName = MatterNames.getPromotedName(clusterId, attrId);
            if (promotedName != null) {
                putTag(placementPrefix + "/" + promotedName, dataType, tagValue);
                continue;
            }

            // 2. Device info attribute (under DeviceInfo/ folder)
            String infoName = MatterNames.getDeviceInfoName(clusterId, attrId);
            if (infoName != null) {
                putTag(placementPrefix + "/DeviceInfo/" + infoName, dataType, tagValue);
                continue;
            }

            // 3. Battery attribute (under Battery/ folder)
            if (clusterId == 47) {
                String batteryName = MatterNames.getBatteryAttributeName(attrId);
                if (batteryName != null) {
                    Object batteryValue = convertBatteryValue(attrId, tagValue);
                    putTag(placementPrefix + "/Battery/" + batteryName, inferDataType(batteryValue), batteryValue);
                    continue;
                }
            }

            // 4. Skip global attributes
            if (MatterNames.isGlobalAttribute(attrId)) continue;

            // 5. Skip remaining infrastructure endpoint attributes
            if (isInfra) continue;

            // 6. Skip non-allowed clusters
            if (!MatterNames.isAllowedCluster(clusterId)) continue;

            // 7. Regular filtered tag
            putTag(prefix + "/Nodes/" + readablePath, dataType, tagValue);
        }

        // Rebuild raw data if it was previously enabled
        if (wasRawDataEnabled) {
            buildRawDataTags(nodeId);
        }

        updateTagValue("Server/NodeCount", nodeFolderNames.size());
    }

    private void removeNodeTags(long nodeId) {
        String prefix = nodePrefix(nodeId);

        tags.keySet().removeIf(k -> k.equals(prefix) || k.startsWith(prefix + "/"));
        childIndex.keySet().removeIf(k -> k.equals(prefix) || k.startsWith(prefix + "/"));

        String folderName = nodeFolderNames.remove(nodeId);
        Set<String> rootChildren = childIndex.get("");
        if (rootChildren != null) {
            rootChildren.remove(folderName != null ? folderName : nodeLabel(nodeId));
        }
        if (folderName != null) {
            folderNameToNodeId.remove(folderName);
        }

        numericToReadable.remove(nodeId);
        readableToNumeric.remove(nodeId);
        endpointNameCache.remove(nodeId);
        endpointDeviceTypeIdCache.remove(nodeId);
        rawDataEnabled.remove(nodeId);
        nodeAttributeCache.remove(nodeId);

        updateTagValue("Server/NodeCount", nodeFolderNames.size());
    }

    private void configureAndUpdateAttributeTag(long nodeId, String numericPath, Object value) {
        // Update attribute cache first so label computations see the latest value
        ConcurrentHashMap<String, Object> cache = nodeAttributeCache.get(nodeId);
        if (cache != null && value != null) {
            cache.put(numericPath, value);
        }

        if (isNodeLabelAttribute(numericPath) && value instanceof String) {
            // Check if top-level node folder needs renaming
            String currentFolderName = nodeFolderNames.get(nodeId);
            if (currentFolderName != null) {
                Map<String, Object> attrs = cache != null ? cache : Map.of(numericPath, value);
                String newFolderName = computeNodeFolderName(nodeId, attrs);
                if (!currentFolderName.equals(newFolderName)) {
                    renameNodeFolder(nodeId, currentFolderName, newFolderName);
                }
            }

            // Check if endpoint sub-folder needs renaming
            String[] pathParts = numericPath.split("/");
            if (pathParts.length == 3) {
                String epId = pathParts[0];
                ConcurrentHashMap<String, String> epNameCache = endpointNameCache.get(nodeId);
                if (epNameCache != null) {
                    String oldEpName = epNameCache.get(epId);
                    if (oldEpName != null) {
                        String newLabel = ((String) value).replace("/", "_").strip();
                        if (!newLabel.isEmpty() && !oldEpName.equals(newLabel)) {
                            renameEndpointFolder(nodeId, epId, oldEpName, newLabel);
                        }
                    }
                }
            }
        }

        String prefix = nodePrefix(nodeId);
        DataType dataType = inferDataType(value);
        Object tagValue = convertValue(value);

        String[] parts = numericPath.split("/");
        if (parts.length != 3) return;

        String epId = parts[0];
        int clusterId = Integer.parseInt(parts[1]);
        int attrId = Integer.parseInt(parts[2]);

        ConcurrentHashMap<String, String> epNames = endpointNameCache.get(nodeId);
        String epName = (epNames != null) ? epNames.getOrDefault(epId, "Endpoint_" + epId) : "Endpoint_" + epId;
        String clusterName = MatterNames.clusterName(clusterId);
        String attrName = MatterNames.attributeName(clusterId, attrId);

        String readablePath = epName + "/" + clusterName + "/" + attrName;

        // Update bidirectional maps
        ConcurrentHashMap<String, String> n2r = numericToReadable.computeIfAbsent(nodeId, k -> new ConcurrentHashMap<>());
        n2r.put(numericPath, readablePath);
        readableToNumeric.computeIfAbsent(nodeId, k -> new ConcurrentHashMap<>()).put(readablePath, numericPath);

        // Update raw data tag if enabled
        if (rawDataEnabled.getOrDefault(nodeId, false)) {
            putTag(prefix + "/Raw Data/" + readablePath, dataType, tagValue);
        }

        // Determine placement prefix based on endpoint type
        boolean isInfra = false;
        ConcurrentHashMap<String, Integer> epDeviceTypes = endpointDeviceTypeIdCache.get(nodeId);
        if (epDeviceTypes != null) {
            Integer deviceTypeId = epDeviceTypes.get(epId);
            if (deviceTypeId != null && MatterNames.isInfrastructureDeviceType(deviceTypeId)) {
                isInfra = true;
            }
        }
        String placementPrefix = isInfra ? prefix : prefix + "/Nodes/" + epName;

        // Apply filtering

        // 1. Promoted attribute
        String promotedName = MatterNames.getPromotedName(clusterId, attrId);
        if (promotedName != null) {
            putTag(placementPrefix + "/" + promotedName, dataType, tagValue);
            return;
        }

        // 2. Device info attribute
        String infoName = MatterNames.getDeviceInfoName(clusterId, attrId);
        if (infoName != null) {
            putTag(placementPrefix + "/DeviceInfo/" + infoName, dataType, tagValue);
            return;
        }

        // 3. Battery attribute
        if (clusterId == 47) {
            String batteryName = MatterNames.getBatteryAttributeName(attrId);
            if (batteryName != null) {
                Object batteryValue = convertBatteryValue(attrId, tagValue);
                putTag(placementPrefix + "/Battery/" + batteryName, inferDataType(batteryValue), batteryValue);
                return;
            }
        }

        // 4. Skip global attributes
        if (MatterNames.isGlobalAttribute(attrId)) return;

        // 5. Skip remaining infrastructure endpoint attributes
        if (isInfra) return;

        // 6. Skip non-allowed clusters
        if (!MatterNames.isAllowedCluster(clusterId)) return;

        // 7. Regular filtered tag
        putTag(prefix + "/Nodes/" + readablePath, dataType, tagValue);
    }

    // ---- Write handling ----

    private void handleAttributeWrite(String tagPath, Object value) throws Exception {
        String[] parts = tagPath.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid tag path: " + tagPath);
        }

        // Root-level commands live outside the node folders, so resolve them first.
        if (CMD.equals(parts[0])) {
            handleRootCommandWrite(parts, value);
            return;
        }

        String folderName = parts[0];
        Long nodeIdObj = folderNameToNodeId.get(folderName);
        if (nodeIdObj == null) {
            throw new IllegalArgumentException("Cannot resolve node ID from folder: " + folderName);
        }
        long nodeId = nodeIdObj;

        // ShowRawData toggle
        if (parts.length == 2 && "ShowRawData".equals(parts[1])) {
            handleShowRawDataWrite(nodeId, value);
            return;
        }

        if (parts.length == 3 && CMD.equals(parts[1])) {
            handleNodeCommandWrite(nodeId, parts[2], value);
            return;
        }

        // Determine the readable path for the attribute
        String readablePath;
        if (parts.length == 5 && ("Nodes".equals(parts[1]) || "Raw Data".equals(parts[1]))) {
            // Nodes path: {folderName}/Nodes/{epName}/{clusterName}/{attrName}
            // Raw data path: {folderName}/Raw Data/{epName}/{clusterName}/{attrName}
            readablePath = parts[2] + "/" + parts[3] + "/" + parts[4];
        } else {
            throw new IllegalArgumentException("Tag is read-only or invalid: " + tagPath);
        }

        ConcurrentHashMap<String, String> r2n = readableToNumeric.get(nodeId);
        if (r2n == null) {
            throw new IllegalStateException("No attribute mappings for node " + nodeId);
        }
        String numericPath = r2n.get(readablePath);
        if (numericPath == null) {
            throw new IllegalArgumentException("Cannot resolve numeric path for: " + readablePath);
        }

        if (matterClient != null && matterClient.isConnected()) {
            matterClient.writeAttribute(nodeId, numericPath, value);
            attributeWritesSent.incrementAndGet();
            updateTagValue("Server/AttributeWritesSent", attributeWritesSent.get());
        } else {
            throw new IllegalStateException("Not connected to Matter server");
        }
    }

    // ---- Commands ----

    /**
     * Commands are write-to-trigger: any truthy write runs the command and the tag snaps back to
     * false. They run on the execution manager because commissioning can block for the whole
     * client timeout, and {@code writeAsync} must not stall a tag write thread that long.
     */
    private void handleRootCommandWrite(String[] parts, Object value) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unknown command tag: " + String.join("/", parts));
        }
        String command = parts[1];

        if ("CommissionCode".equals(command)) {
            commissionCode = value == null ? "" : String.valueOf(value);
            updateTagValue(CMD + "/CommissionCode", commissionCode);
            return;
        }
        if (!isTriggered(value)) {
            return;
        }

        switch (command) {
            case "Commission" -> runCommand("Commission", () -> {
                String code = commissionCode;
                if (code == null || code.isBlank()) {
                    throw new IllegalStateException("Write the pairing code to " + CMD
                            + "/CommissionCode before triggering " + CMD + "/Commission");
                }
                MatterNodeData node = requireClient().commissionWithCode(code);
                return "Commissioned node " + Long.toUnsignedString(node.getNodeId());
            });
            case "Discover" -> runCommand("Discover", () -> {
                var found = requireClient().discoverCommissionableNodes();
                return found.isEmpty() ? "No commissionable devices found"
                        : found.size() + " commissionable device(s): " + gson.toJson(found);
            });
            case "Refresh" -> runCommand("Refresh", () -> {
                var nodes = requireClient().startListening();
                for (MatterNodeData node : nodes) {
                    buildNodeTags(node);
                }
                return "Refreshed " + nodes.size() + " node(s)";
            });
            default -> throw new IllegalArgumentException("Unknown command tag: " + CMD + "/" + command);
        }
    }

    private void handleNodeCommandWrite(long nodeId, String command, Object value) {
        String label = nodeLabel(nodeId);
        if (!isTriggered(value)) {
            return;
        }
        switch (command) {
            case "OpenCommissioningWindow" -> runCommand(label + " OpenCommissioningWindow", () -> {
                var params = requireClient().openCommissioningWindow(nodeId);
                updateTagValue(CMD + "/LastQRCode", nullToEmpty(params.getSetupQrCode()));
                updateTagValue(CMD + "/LastManualCode", nullToEmpty(params.getSetupManualCode()));
                return "Commissioning window open; manual code "
                        + nullToEmpty(params.getSetupManualCode());
            });
            case "Interview" -> runCommand(label + " Interview", () -> {
                requireClient().interviewNode(nodeId);
                return "Interview complete";
            });
            case "Ping" -> runCommand(label + " Ping", () -> {
                var result = requireClient().pingNode(nodeId);
                return "Ping: " + result;
            });
            case "CheckUpdate" -> runCommand(label + " CheckUpdate", () -> {
                var update = requireClient().checkNodeUpdate(nodeId);
                return update == null ? "No update available" : "Update available: " + gson.toJson(update);
            });
            default -> throw new IllegalArgumentException(
                    "Unknown command tag: " + label + "/" + CMD + "/" + command);
        }
    }

    /** A command tag fires on any truthy write; booleans, numbers and strings are all accepted. */
    private static boolean isTriggered(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.doubleValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private interface Command {
        String run() throws Exception;
    }

    private void runCommand(String label, Command command) {
        updateTagValue(CMD + "/LastCommand", label);
        context.getExecutionManager().executeOnce(() -> {
            try {
                String result = command.run();
                logger.info("Command '{}' on '{}': {}", label, name, result);
                updateTagValue(CMD + "/LastResult", result);
                updateTagValue(CMD + "/LastError", "");
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                logger.warn("Command '{}' on '{}' failed: {}", label, name, message, e);
                updateTagValue(CMD + "/LastResult", "");
                updateTagValue(CMD + "/LastError", message);
            }
        });
    }

    // ---- Raw data toggle ----

    private void handleShowRawDataWrite(long nodeId, Object value) {
        boolean enabled = Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
        rawDataEnabled.put(nodeId, enabled);

        String prefix = nodePrefix(nodeId);
        updateTagValue(prefix + "/ShowRawData", enabled);

        if (enabled) {
            buildRawDataTags(nodeId);
        } else {
            removeRawDataTags(nodeId);
        }
    }

    private void buildRawDataTags(long nodeId) {
        ConcurrentHashMap<String, Object> attributes = nodeAttributeCache.get(nodeId);
        if (attributes == null) return;

        String prefix = nodePrefix(nodeId);
        ConcurrentHashMap<String, String> n2r = numericToReadable.get(nodeId);
        if (n2r == null) return;

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String numericPath = entry.getKey();
            String readablePath = n2r.get(numericPath);
            if (readablePath == null) continue;

            DataType dataType = inferDataType(entry.getValue());
            Object tagValue = convertValue(entry.getValue());
            putTag(prefix + "/Raw Data/" + readablePath, dataType, tagValue);
        }
    }

    private void removeRawDataTags(long nodeId) {
        String prefix = nodePrefix(nodeId);
        String rawPrefix = prefix + "/Raw Data";

        tags.keySet().removeIf(k -> k.equals(rawPrefix) || k.startsWith(rawPrefix + "/"));
        childIndex.keySet().removeIf(k -> k.equals(rawPrefix) || k.startsWith(rawPrefix + "/"));

        Set<String> nodeChildren = childIndex.get(prefix);
        if (nodeChildren != null) {
            nodeChildren.remove("Raw Data");
        }
    }

    // ---- Tag tree helpers ----

    private void putTag(String path, DataType dataType, Object value) {
        QualifiedValue qv = new BasicQualifiedValue(value, QualityCode.Good);
        TagNode existing = tags.get(path);
        if (existing != null && !existing.isFolder) {
            existing.currentValue = qv;
            notifySubscribers(path, qv);
            return;
        }

        tags.put(path, new TagNode(dataType, false, qv));
        registerInTree(path);
        notifySubscribers(path, qv);
    }

    private void putFolder(String path) {
        if (tags.containsKey(path)) return;
        tags.put(path, new TagNode(DataType.String, true,
                new BasicQualifiedValue(null, QualityCode.Good)));
        registerInTree(path);
    }

    private void registerInTree(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentPath = path.substring(0, lastSlash);
            String childName = path.substring(lastSlash + 1);
            putFolder(parentPath);
            childIndex.computeIfAbsent(parentPath, k -> ConcurrentHashMap.newKeySet()).add(childName);
        } else {
            childIndex.computeIfAbsent("", k -> ConcurrentHashMap.newKeySet()).add(path);
        }
    }

    private void updateTagValue(String path, Object value) {
        TagNode node = tags.get(path);
        if (node != null) {
            QualifiedValue qv = new BasicQualifiedValue(value, QualityCode.Good);
            node.currentValue = qv;
            notifySubscribers(path, qv);
        }
    }

    private void onSubscriptionChanged(TagSubscriptionChangeEvent event) {
        for (TagSubscription sub : event.getAddedSubscriptions()) {
            String path = tagPathToString(sub.getPath());
            TagNode node = tags.get(path);
            if (node != null) {
                try {
                    sub.getListener().tagChanged(new TagChangeEvent(sub.getPath(), node.currentValue));
                } catch (Exception e) {
                    logger.debug("Error pushing initial value for {}", path, e);
                }
            }
        }
    }

    private void notifySubscribers(String path, QualifiedValue value) {
        if (subscriptionModel == null) return;

        Collection<TagSubscription> subs = subscriptionModel.getSubscriptions(name);
        if (subs == null || subs.isEmpty()) return;

        for (TagSubscription sub : subs) {
            if (tagPathToString(sub.getPath()).equals(path)) {
                try {
                    sub.getListener().tagChanged(new TagChangeEvent(sub.getPath(), value));
                } catch (Exception e) {
                    logger.debug("Error notifying subscriber for {}", path, e);
                }
            }
        }
    }

    private String tagPathToString(TagPath path) {
        if (path == null || path.getPathLength() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.getPathLength(); i++) {
            if (i > 0) sb.append('/');
            sb.append(path.getPathComponent(i));
        }
        return sb.toString();
    }

    // ---- Utilities ----

    /** Node IDs are unsigned 64-bit; {@code Long.toString} would render matter.js test nodes negative. */
    private static String nodeLabel(long nodeId) {
        return "Node " + Long.toUnsignedString(nodeId);
    }

    private String nodePrefix(long nodeId) {
        return nodeFolderNames.getOrDefault(nodeId, nodeLabel(nodeId));
    }

    private static String extractNodeLabel(Map<String, Object> attributes) {
        // Check endpoint 0 first (backward compatibility)
        for (String key : new String[]{"0/40/5", "0/57/5"}) {
            Object val = attributes.get(key);
            if (val instanceof String s && !s.isBlank()) {
                return s;
            }
        }

        // For bridged devices, check non-zero endpoints
        TreeMap<Integer, String> epLabels = new TreeMap<>();
        TreeMap<Integer, Boolean> nonInfraEps = new TreeMap<>();

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String[] parts = entry.getKey().split("/");
            if (parts.length != 3) continue;
            int ep, cluster, attr;
            try {
                ep = Integer.parseInt(parts[0]);
                cluster = Integer.parseInt(parts[1]);
                attr = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) { continue; }
            if (ep == 0) continue;

            if (cluster == 29 && attr == 0) {
                Integer dtId = MatterNames.resolveEndpointDeviceTypeId(entry.getValue());
                if (dtId == null || !MatterNames.isInfrastructureDeviceType(dtId)) {
                    nonInfraEps.put(ep, true);
                }
            }

            if ((cluster == 40 || cluster == 57) && attr == 5
                    && entry.getValue() instanceof String s && !s.isBlank()) {
                epLabels.put(ep, s);
            }
        }

        // Only use a non-zero endpoint's label for single non-infrastructure endpoint nodes
        if (nonInfraEps.size() == 1) {
            return epLabels.get(nonInfraEps.firstKey());
        }

        return null;
    }

    private String computeNodeFolderName(long nodeId, Map<String, Object> attributes) {
        String label = extractNodeLabel(attributes);
        if (label == null) {
            return nodeLabel(nodeId);
        }
        String sanitized = label.replace("/", "_").strip();
        if (sanitized.equals("Server") || sanitized.equals(CMD)) {
            // Reserved root folder names; disambiguate rather than shadow them.
            return sanitized + " (" + nodeLabel(nodeId) + ")";
        }
        if (sanitized.isEmpty()) {
            return nodeLabel(nodeId);
        }
        Long existing = folderNameToNodeId.get(sanitized);
        if (existing != null && existing != nodeId) {
            sanitized = sanitized + " (Node " + nodeId + ")";
        }
        return sanitized;
    }

    private static boolean isNodeLabelAttribute(String numericPath) {
        String[] parts = numericPath.split("/");
        if (parts.length != 3) return false;
        int cluster = Integer.parseInt(parts[1]);
        int attr = Integer.parseInt(parts[2]);
        return (cluster == 40 || cluster == 57) && attr == 5;
    }

    private void renameNodeFolder(long nodeId, String oldName, String newName) {
        String oldPrefix = oldName;
        String newPrefix = newName;

        List<String> oldPaths = new ArrayList<>();
        for (String key : tags.keySet()) {
            if (key.equals(oldPrefix) || key.startsWith(oldPrefix + "/")) {
                oldPaths.add(key);
            }
        }

        for (String oldPath : oldPaths) {
            String newPath = newPrefix + oldPath.substring(oldPrefix.length());
            TagNode node = tags.remove(oldPath);
            if (node != null) {
                tags.put(newPath, node);
            }
            Set<String> children = childIndex.remove(oldPath);
            if (children != null) {
                childIndex.put(newPath, children);
            }
        }

        Set<String> rootChildren = childIndex.get("");
        if (rootChildren != null) {
            rootChildren.remove(oldName);
            rootChildren.add(newName);
        }

        nodeFolderNames.put(nodeId, newName);
        folderNameToNodeId.remove(oldName);
        folderNameToNodeId.put(newName, nodeId);
    }

    private void renameEndpointFolder(long nodeId, String epId, String oldEpName, String newEpName) {
        String nodeFolder = nodePrefix(nodeId);

        // Rename tags under Nodes/{oldEpName} and Raw Data/{oldEpName}
        for (String section : new String[]{"Nodes", "Raw Data"}) {
            String oldPrefix = nodeFolder + "/" + section + "/" + oldEpName;
            String newPrefix = nodeFolder + "/" + section + "/" + newEpName;

            List<String> oldPaths = new ArrayList<>();
            for (String key : tags.keySet()) {
                if (key.equals(oldPrefix) || key.startsWith(oldPrefix + "/")) {
                    oldPaths.add(key);
                }
            }

            for (String oldPath : oldPaths) {
                String newPath = newPrefix + oldPath.substring(oldPrefix.length());
                TagNode node = tags.remove(oldPath);
                if (node != null) {
                    tags.put(newPath, node);
                }
                Set<String> children = childIndex.remove(oldPath);
                if (children != null) {
                    childIndex.put(newPath, children);
                }
            }

            // Update parent's child set
            Set<String> sectionChildren = childIndex.get(nodeFolder + "/" + section);
            if (sectionChildren != null) {
                sectionChildren.remove(oldEpName);
                sectionChildren.add(newEpName);
            }
        }

        // Update endpoint name cache
        ConcurrentHashMap<String, String> epNames = endpointNameCache.get(nodeId);
        if (epNames != null) {
            epNames.put(epId, newEpName);
        }

        // Update bidirectional maps: replace old endpoint name prefix with new one
        ConcurrentHashMap<String, String> n2r = numericToReadable.get(nodeId);
        ConcurrentHashMap<String, String> r2n = readableToNumeric.get(nodeId);
        if (n2r != null && r2n != null) {
            String oldReadablePrefix = oldEpName + "/";
            String newReadablePrefix = newEpName + "/";
            List<Map.Entry<String, String>> toUpdate = new ArrayList<>();
            for (Map.Entry<String, String> entry : n2r.entrySet()) {
                if (entry.getValue().startsWith(oldReadablePrefix)) {
                    toUpdate.add(entry);
                }
            }
            for (Map.Entry<String, String> entry : toUpdate) {
                String numPath = entry.getKey();
                String oldReadable = entry.getValue();
                String newReadable = newReadablePrefix + oldReadable.substring(oldReadablePrefix.length());
                n2r.put(numPath, newReadable);
                r2n.remove(oldReadable);
                r2n.put(newReadable, numPath);
            }
        }
    }

    private static DataType inferDataType(Object value) {
        if (value instanceof Boolean) return DataType.Boolean;
        if (value instanceof Number) return DataType.Float8;
        return DataType.String;
    }

    private Object convertValue(Object value) {
        if (value == null) return "";
        if (value instanceof Boolean || value instanceof Number || value instanceof String) return value;
        return gson.toJson(value);
    }

    /**
     * Converts raw Matter battery attribute values to human-readable units.
     * - BatVoltage (11): millivolts -> volts
     * - BatPercentRemaining (12): half-percent units (0-200) -> percent (0-100)
     */
    private static Object convertBatteryValue(int attrId, Object value) {
        if (!(value instanceof Number num)) return value;
        return switch (attrId) {
            case 11 -> num.doubleValue() / 1000.0;   // mV -> V
            case 12 -> num.doubleValue() / 2.0;       // half-percent -> percent
            default -> value;
        };
    }

}
