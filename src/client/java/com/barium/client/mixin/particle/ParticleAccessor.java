package com.barium.client.mixin.particle;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("x")
    double barium$getX();

    @Accessor("y")
    double barium$getY();

    @Accessor("z")
    double barium$getZ();
}