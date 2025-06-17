package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos; // Import necessário
import net.minecraft.util.math.Vec3d; // Import necessário
import net.minecraft.util.shape.VoxelShape; // Import necessário
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
     * Alvo: ClientWorld.tickEntity(Entity)
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

    /**
     * Otimização de Partículas de Ambiente.
     * Alvo: ClientWorld.doRandomBlockDisplayTicks(int, int, int)
     */
    @Inject(method = "doRandomBlockDisplayTicks(III)V", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int centerX, int centerY, int centerZ, CallbackInfo ci) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;

        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel();
        }
    }

    /**
     * Otimização de Partículas de Explosão.
     * Alvo: ClientWorld.addParticle(BlockPos, ParticleEffect, VoxelShape, double)
     * Esta é a sobrecarga mais provável para interceptar a criação de partículas genéricas.
     */
    @Inject(
        // Descritor para: addParticle(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y)
        method = "addParticle(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/util/shape/VoxelShape;D)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y, CallbackInfo ci) {
        // `this` aqui é um ClientWorld.
        // Precisamos verificar se ele é um cliente e se a otimização está ativa.
        // A verificação `!world.isClient` não é necessária porque estamos no ClientWorld.
        if (!BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        // Verifica se o tipo de partícula é de explosão
        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            // 1 em 4 chance de cancelar a adição da partícula
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel();
            }
        }
    }
}