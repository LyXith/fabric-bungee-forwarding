package com.runningbird.fabricbungeeforwarding.net;

import com.mojang.authlib.properties.Property;

import java.net.InetSocketAddress;
import java.util.UUID;

public interface ForwardedDataHolder {
    boolean bff$hasForwardedData();

    void bff$setForwardedData(UUID uuid, Property[] properties, String hostname, InetSocketAddress address);

    UUID bff$getForwardedUuid();

    Property[] bff$getForwardedProfile();

    String bff$getForwardedHostname();
}
