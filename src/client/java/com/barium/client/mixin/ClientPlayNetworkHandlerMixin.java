package com.barium.client.mixin;

import com.barium.client.optimization.NetworkPacketOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityTrackerUpdate", at = @At("HEAD"), cancellable = true)
    private void barium$throttleFarEntityTrackerUpdates(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        Integer entityId = NetworkPacketOptimizer.extractEntityId(packet);
        if (entityId == null) return;

        Entity entity = client.world.getEntityById(entityId);
        if (NetworkPacketOptimizer.shouldThrottleTrackerUpdate(client, entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "onEntityAttributes", at = @At("HEAD"), cancellable = true)
    private void barium$throttleFarEntityAttributeUpdates(EntityAttributesS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        Integer entityId = NetworkPacketOptimizer.extractEntityId(packet);
        if (entityId == null) return;

        Entity entity = client.world.getEntityById(entityId);
        if (NetworkPacketOptimizer.shouldThrottleAttributeUpdate(client, entity)) {
            ci.cancel();
        }
    }
}
