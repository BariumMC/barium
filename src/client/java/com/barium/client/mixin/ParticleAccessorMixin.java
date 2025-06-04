package com.barium.client.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public abstract class ParticleAccessorMixin {

    @Shadow protected ClientWorld world;

    public ClientWorld barium$getWorld() {
        return this.world;
    }
}
