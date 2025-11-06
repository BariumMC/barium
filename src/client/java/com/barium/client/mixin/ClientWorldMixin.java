package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    /**
     * Otimização de Tick de Entidade.
     * Alvo: ClientWorld.tickEntity(Entity)
     */
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void barium$cullDistantEntityTicks(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;
        if (entity.isPlayer() || entity.hasPassengers() || entity.getVehicle() != null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // CORREÇÃO 25w45a: Entity.getPos() foi removido. Use entity.squaredDistanceTo(otherEntity).
        double distanceSq = entity.squaredDistanceTo(client.player);
        if (distanceSq > BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ) {
            if (entity.age % 4 != 0) {
                ci.cancel();
            }
        }
    }

    /**
     * Otimização de Partículas de Ambiente.
     * Alvo: ClientWorld.doRandomBlockDisplayTicks(int, int, int)
     */
    @Inject(method = "doRandomBlockDisplayTicks", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int centerX, int centerY, int centerZ, CallbackInfo ci) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;

        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel();
        }
    }
}