package com.runningbird.fabricbungeeforwarding.mixin;

import com.mojang.authlib.properties.Property;
import com.runningbird.fabricbungeeforwarding.net.ForwardedDataHolder;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.net.InetSocketAddress;
import java.util.UUID;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements ForwardedDataHolder {
    @Unique
    private UUID bff$forwardedUuid;
    @Unique
    private Property[] bff$forwardedProfile;
    @Unique
    private String bff$forwardedHostname;

    @Override
    public boolean bff$hasForwardedData() {
        return this.bff$forwardedUuid != null;
    }

    @Override
    public void bff$setForwardedData(UUID uuid, Property[] properties, String hostname, InetSocketAddress address) {
        this.bff$forwardedUuid = uuid;
        this.bff$forwardedProfile = properties;
        this.bff$forwardedHostname = hostname;
        ((ConnectionAccessor) this).bff$setAddress(address);
    }

    @Override
    public UUID bff$getForwardedUuid() {
        return this.bff$forwardedUuid;
    }

    @Override
    public Property[] bff$getForwardedProfile() {
        return this.bff$forwardedProfile;
    }

    @Override
    public String bff$getForwardedHostname() {
        return this.bff$forwardedHostname;
    }
}
