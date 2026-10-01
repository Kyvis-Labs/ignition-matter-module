package com.matter.ignition.gateway;

import java.util.List;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.config.ExtensionPoint;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatterTagProviderGatewayHook extends AbstractGatewayModuleHook {

    static final String MODULE_ID = "com.kyvislabs.matter.ignition.matter-tag-provider";

    private Logger logger;

    @Override
    public void setup(GatewayContext context) {
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void startup(LicenseState activationState) {
        BundleUtil.get().addBundle(MatterTagProviderSettings.class);
        // The tag provider list renders TagProviderTypes.<typeId>.Display/.Description; without
        // this bundle the gateway shows the raw key instead of a name.
        BundleUtil.get().addBundle(
                "TagProviderTypes",
                getClass().getClassLoader(),
                "com/matter/ignition/gateway/TagProviderTypes");
        logger.info("Matter Tag Provider module started.");
    }

    @Override
    public void shutdown() {
        BundleUtil.get().removeBundle(MatterTagProviderSettings.class);
        BundleUtil.get().removeBundle("TagProviderTypes");
        logger.info("Matter Tag Provider module stopped.");
    }

    /**
     * Exposes {@code system.matter.*} in gateway scope — Perspective, gateway event scripts and tag
     * event scripts. Commissioning and node management have no natural tag representation, so they
     * live here; the per-node command tags cover the common operations for operators.
     */
    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);
        manager.addScriptModule("system.matter", new MatterScriptModule());
    }

    @Override
    public List<? extends ExtensionPoint<?>> getExtensionPoints() {
        return List.of(new MatterTagProviderExtensionPoint());
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }

    @Override
    public boolean isMakerEditionCompatible() {
        return true;
    }
}
