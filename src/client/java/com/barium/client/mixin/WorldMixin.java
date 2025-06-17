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
     * CORREÇÃO PARA 1.21.6+: A assinatura correta do método pode ter mudado.
     * Verifique a assinatura exata para 'addParticle' na sua versão do Minecraft.
     * Com base em versões recentes, a assinatura pode ser mais ou menos assim,
     * mas o tipo 'ParticleEffect' é a parte crucial a ser verificada.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDddd)V", // Assinatura corrigida
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$reduceExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        World self = (World)(Object)this;

        if (!self.isClient || !BariumConfig.C.ENABLE_EXPLOSION_PARTICLE_REDUCTION) {
            return;
        }

        // Verifique se o tipo de partícula é de explosão
        if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                ci.cancel();
            }
        }
    }
}