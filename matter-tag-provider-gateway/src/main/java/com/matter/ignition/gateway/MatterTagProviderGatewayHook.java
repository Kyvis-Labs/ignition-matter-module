package com.matter.ignition.gateway;

import java.util.List;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
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
        logger.info("Matter Tag Provider module started.");
    }

    @Override
    public void shutdown() {
        BundleUtil.get().removeBundle(MatterTagProviderSettings.class);
        logger.info("Matter Tag Provider module stopped.");
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
