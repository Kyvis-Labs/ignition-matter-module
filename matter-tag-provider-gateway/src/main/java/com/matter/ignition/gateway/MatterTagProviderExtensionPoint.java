package com.matter.ignition.gateway;

import java.util.Optional;

import com.inductiveautomation.ignition.gateway.config.DecodedResource;
import com.inductiveautomation.ignition.gateway.config.ExtensionPoint;
import com.inductiveautomation.ignition.gateway.config.ExtensionPointConfig;
import com.inductiveautomation.ignition.gateway.config.ValidationErrors;
import com.inductiveautomation.ignition.gateway.dataroutes.openapi.SchemaUtil;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.tags.api.TagProviderExtensionPoint;
import com.inductiveautomation.ignition.gateway.tags.api.TagProviderProfileConfig;
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagProvider;
import com.inductiveautomation.ignition.gateway.web.nav.ExtensionPointResourceForm;
import com.inductiveautomation.ignition.gateway.web.nav.WebUiComponent;

public class MatterTagProviderExtensionPoint
        extends TagProviderExtensionPoint<MatterTagProviderSettings> {

    public static final String TYPE_ID = "MatterServer";

    public MatterTagProviderExtensionPoint() {
        super(TYPE_ID);
    }

    @Override
    public GatewayTagProvider createNewProvider(
            GatewayContext context,
            DecodedResource<ExtensionPointConfig<TagProviderProfileConfig, ?>> resource) {
        var settings = getSettings(resource.config())
                .orElse(MatterTagProviderSettings.DEFAULT);
        return new MatterTagProvider(context, resource.name(), settings);
    }

    @Override
    public Optional<WebUiComponent> getWebUiComponent(ExtensionPoint.ComponentType type) {
        return Optional.of(new ExtensionPointResourceForm(
                resourceType(),
                TYPE_ID,
                "Tag Provider",
                SchemaUtil.fromType(TagProviderProfileConfig.class),
                SchemaUtil.fromType(MatterTagProviderSettings.class)));
    }

    @Override
    public Optional<Class<MatterTagProviderSettings>> settingsType() {
        return Optional.of(MatterTagProviderSettings.class);
    }

    @Override
    public Optional<MatterTagProviderSettings> defaultSettings() {
        return Optional.of(MatterTagProviderSettings.DEFAULT);
    }

    @Override
    protected void validate(MatterTagProviderSettings settings, ValidationErrors.Builder errors) {
        errors.requireNotEmpty("serverUrl", settings.serverUrl());
        errors.checkField(
                settings.serverUrl().startsWith("ws://") || settings.serverUrl().startsWith("wss://"),
                "serverUrl", "Must start with ws:// or wss://");
    }
}
