package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(World.class)
public abstract class WorldMixin {

    /**
     * Injeta no método que adiciona partículas.
     * CORREÇÃO FINAL: Usamos o seletor 'value' para tornar o mixin mais robusto contra
     * pequenas alterações na assinatura do método. O compilador do Mixin irá encontrar o alvo
     * correto com base nos parâmetros do nosso método.
     */
    @Inject(
        at = @At("HEAD"),
        cancellable = true,
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V"
    )
    private void barium$reduceExplosionParticles(ParticleEffect parameters, boolean ignoreRadius, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        World self = (World)(Object)this;

        if (!self.isClient || !BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel();
            }
        }
    }
}