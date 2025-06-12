package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    /**
     * Injeta no início do método que atualiza a lógica de cada entidade no lado do cliente.
     */
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void barium$cullDistantEntityTicks(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;
        if (entity.isPlayer() || entity.hasPassengers() || entity.getVehicle() != null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        double distanceSq = entity.getPos().squaredDistanceTo(client.player.getPos());
        if (distanceSq > BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ) {
            if (entity.age % 4 != 0) {
                ci.cancel();
            }
        }
    }
}