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

    @Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void barium$smartTickCulling(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;
        
        // Jogadores e veículos sempre processam normalmente
        if (entity.isPlayer() || entity.hasPassengers()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double distSq = entity.squaredDistanceTo(client.player);
        
        // Se estiver além de 64 blocos, processa apenas 25% dos ticks
        if (distSq > 4096.0) {
            if (entity.age % 4 != 0) {
                ci.cancel();
            }
        }
    }
}