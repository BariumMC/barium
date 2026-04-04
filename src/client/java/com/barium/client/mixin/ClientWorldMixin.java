package com.barium.client.mixin;

import com.barium.client.optimization.TickOptimizer;
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
    private void barium$adaptiveEntityTickCulling(Entity entity, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (TickOptimizer.shouldSkipEntityTick(entity, client.player, client)) {
            ci.cancel();
        }
    }

    /**
     * Reduz o custo de partículas/blocos ambientais (lava, fumaça, etc.)
     * executando os random display ticks em metade dos ticks do cliente.
     *
     * Isso é aplicado apenas quando a opção REDUCE_AMBIENT_PARTICLES está ativa.
     */
    @Inject(method = "doRandomBlockDisplayTicks", at = @At("HEAD"), cancellable = true)
    private void barium$adaptiveRandomBlockDisplayTicks(CallbackInfo ci) {
        ClientWorld world = (ClientWorld) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (TickOptimizer.shouldSkipRandomBlockDisplayTicks(world, client)) {
            ci.cancel();
        }
    }
}
