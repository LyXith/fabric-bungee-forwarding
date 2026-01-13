package com.runningbird.fabricbungeeforwarding.mixin;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.runningbird.fabricbungeeforwarding.net.ForwardedDataHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
    @Shadow
    @Final
    MinecraftServer server;

    @Shadow
    @Final
    Connection connection;

    @Shadow
    abstract void startClientVerification(GameProfile gameProfile);

    @Inject(
        method = "handleHello(Lnet/minecraft/network/protocol/login/ServerboundHelloPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/UUIDUtil;createOfflineProfile(Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;"
        ),
        cancellable = true
    )
    private void bff$enforceForwarding(ServerboundHelloPacket packet, CallbackInfo ci) {
        ForwardedDataHolder holder = (ForwardedDataHolder) this.connection;
        if (!holder.bff$hasForwardedData()) {
            Component message = Component.literal("If you wish to use IP forwarding, please enable it in your proxy config as well!");
            com.runningbird.fabricbungeeforwarding.BungeeForwardingMod.LOGGER.warn("[BFF] Rejecting {}: missing forwarded data", packet.name());
            return;
        }

        try {
            GameProfile profile = this.bff$buildProfile(packet.name(), holder);
            this.startClientVerification(profile);
            com.runningbird.fabricbungeeforwarding.BungeeForwardingMod.LOGGER.debug(
                "[BFF] Built forwarded profile name={} uuid={} props={}",
                ((GameProfileAccessor) (Object) profile).bff$getName(),
                ((GameProfileAccessor) (Object) profile).bff$getId(),
                holder.bff$getForwardedProfile() == null ? 0 : holder.bff$getForwardedProfile().length
            );
        } catch (Exception ex) {
            Component message = Component.literal("Unable to apply forwarded profile from proxy.");
            com.runningbird.fabricbungeeforwarding.BungeeForwardingMod.LOGGER.error("[BFF] Failed to apply forwarded profile for {}: {}", packet.name(), ex.toString(), ex);
        }
    }

    private GameProfile bff$buildProfile(String name, ForwardedDataHolder holder) {
        UUID uuid = holder.bff$getForwardedUuid();
        if (uuid == null) {
            uuid = UUIDUtil.createOfflinePlayerUUID(name);
        }

        com.google.common.collect.Multimap<String, Property> backing = HashMultimap.create();
        Property[] forwarded = holder.bff$getForwardedProfile();
        if (forwarded != null) {
            for (Property property : forwarded) {
                if (property != null && property.name() != null && !property.name().isBlank()) {
                    backing.put(property.name(), property);
                }
            }
        }

        GameProfile profile = new GameProfile(uuid, name);
        PropertyMap propertyMap = this.bff$createPropertyMap(backing);
        ((GameProfileAccessor) (Object) profile).bff$setProperties(propertyMap);
        return profile;
    }

    private PropertyMap bff$createPropertyMap(com.google.common.collect.Multimap<String, Property> backing) {
        // Try ctor(PropertyMap(Multimap)) if available (1.21.10+), otherwise fall back to no-arg.
        try {
            java.lang.reflect.Constructor<PropertyMap> ctor = PropertyMap.class.getDeclaredConstructor(com.google.common.collect.Multimap.class);
            ctor.setAccessible(true);
            return ctor.newInstance(backing);
        } catch (NoSuchMethodException ignored) {
            // fall through
        } catch (Exception ex) {
            throw new RuntimeException("Unable to construct PropertyMap", ex);
        }

        try {
            java.lang.reflect.Constructor<PropertyMap> ctor = PropertyMap.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            PropertyMap propertyMap = ctor.newInstance();
            backing.asMap().forEach((key, values) -> {
                for (Property value : values) {
                    if (value != null) {
                        propertyMap.put(key, value);
                    }
                }
            });
            return propertyMap;
        } catch (Exception ex) {
            throw new RuntimeException("Unable to construct PropertyMap (no-arg)", ex);
        }
    }
}
