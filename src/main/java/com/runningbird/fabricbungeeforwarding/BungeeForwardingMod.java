package com.runningbird.fabricbungeeforwarding;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BungeeForwardingMod implements DedicatedServerModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-bungee-forwarding");

    @Override
    public void onInitializeServer() {
        LOGGER.info("Fabric Bungee Forwarding loaded; plain logins without proxy data will be rejected.");
    }
}
