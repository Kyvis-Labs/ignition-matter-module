package com.matter.ignition.gateway;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the running Matter tag providers by name.
 *
 * <p>A gateway can host several Matter tag providers (one per Matter server), and the scripting
 * layer addresses them by provider name. The providers are created by
 * {@link MatterTagProviderExtensionPoint}, not by the module hook, so there is no other path from
 * a script function to a live provider.
 */
final class MatterProviderRegistry {

    private static final ConcurrentHashMap<String, MatterTagProvider> PROVIDERS =
            new ConcurrentHashMap<>();

    private MatterProviderRegistry() {
    }

    static void register(String name, MatterTagProvider provider) {
        PROVIDERS.put(name, provider);
    }

    static void unregister(String name) {
        PROVIDERS.remove(name);
    }

    /** Provider names currently running, for {@code system.matter.getProviders()}. */
    static List<String> names() {
        return PROVIDERS.keySet().stream().sorted().toList();
    }

    /**
     * @throws IllegalArgumentException naming the known providers, since the usual cause is a typo
     *                                  in a script or a provider that has not started yet.
     */
    static MatterTagProvider require(String name) {
        MatterTagProvider provider = name == null ? null : PROVIDERS.get(name);
        if (provider == null) {
            throw new IllegalArgumentException(
                    "No Matter tag provider named '" + name + "'. Running providers: " + names());
        }
        return provider;
    }
}
