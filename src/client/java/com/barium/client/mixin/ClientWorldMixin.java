package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    /**
     * Otimização de Tick de Entidade.
     */
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void barium$cullDistantEntityTicks(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;
        if (entity.isPlayer() || entity.hasPassengers() || entity.getVehicle() != null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double distanceSq = entity.getPos().squaredDistanceTo(client.player.getPos());
        if (distanceSq > BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ) {
            // Executa a lógica apenas 1 a cada 4 ticks para entidades distantes
            if (entity.age % 4 != 0) {
                ci.cancel();
            }
        }
    }

    /**
     * Otimização de Partículas de Ambiente.
     */
    @Inject(method = "doRandomBlockDisplayTicks", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int centerX, int centerY, int centerZ, CallbackInfo ci) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;

        // Pula a execução em ticks pares, cortando o custo de CPU pela metade.
        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel();
        }
    }

    /**
     * NOVA LÓGICA: Reduz partículas de explosão.
     * Movido de WorldMixin para ClientWorldMixin para maior estabilidade e correção.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel();
            }
        }
    }
}