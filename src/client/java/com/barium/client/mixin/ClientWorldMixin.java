package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld; // Import correto para a @Mixin
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World; // Import para usar .getTime()
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ClientWorld.class) // <-- Aplicado à classe correta
public abstract class ClientWorldMixin {

    /**
     * Otimização de Tick de Entidade.
     * Alvo: ClientWorld.tickEntity(Entity)
     */
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void barium$cullDistantEntityTicks(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;
        // Ignora jogadores e entidades que estão sendo passageiros/passageiros
        if (entity.isPlayer() || entity.hasPassengers() || entity.getVehicle() != null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return; // Segurança caso o cliente não esteja pronto

        double distanceSq = entity.getPos().squaredDistanceTo(client.player.getPos());
        if (distanceSq > BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ) {
            // Reduz a frequência de tick para entidades distantes.
            // A verificação de idade é uma forma simples de fazer isso (executa a cada 4 ticks).
            if (entity.age % 4 != 0) {
                ci.cancel(); // Cancela o tick desta entidade
            }
        }
    }

    /**
     * Otimização de Partículas de Ambiente.
     * Alvo: ClientWorld.doRandomBlockDisplayTicks(int, int, int)
     */
    @Inject(method = "doRandomBlockDisplayTicks(III)V", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int centerX, int centerY, int centerZ, CallbackInfo ci) {
        // A verificação 'isClient' não é necessária, pois já estamos em ClientWorld.
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;

        // Pula a execução em ticks pares, cortando o custo de CPU pela metade.
        // Usamos `(World)(Object)this` para poder chamar getTime().
        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel(); // Cancela a execução deste método em ticks pares
        }
    }

    /**
     * Otimização de Partículas de Explosão.
     * Alvo: ClientWorld.addParticle(ParticleEffect, DDDDDD)
     * Este método agora está aqui pois pertence ao ClientWorld.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", // Assinatura correta para ClientWorld
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        // Acessamos o mundo como `this` pois estamos dentro de ClientWorld.
        World world = (World)(Object)this;

        // Verifica se estamos no cliente e se a otimização de explosão está ativa
        if (!world.isClient || !BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
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