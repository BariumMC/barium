package com.barium.client.mixin.particle;

import com.barium.config.BariumConfig;
import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleAccessor { // Implementa a interface Accessor

    // Injeta no início do método render() da partícula
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void barium$beforeRender(VertexConsumerProvider vertexConsumers, Camera camera, float tickDelta, CallbackInfo ci) {
        // Se o culling estiver desabilitado, não faz nada e permite a renderização
        if (!BariumConfig.ENABLE_PARTICLE_CULLING) {
            // No need to call ParticleOptimizer.getParticleLOD here if only culling.
            // But if LOD affects rendering, call it here:
            // ParticleOptimizer.getParticleLOD((Particle)(Object)this, camera);
            return;
        }

        // Obtém a posição da partícula usando os accessors
        Vec3d particlePos = new Vec3d(this.barium$getX(), this.barium$getY(), this.barium$getZ());
        Vec3d cameraPos = camera.getPos();

        double distance = particlePos.distanceTo(cameraPos);

        // Culling baseado na distância
        if (distance > BariumConfig.PARTICLE_CULLING_DISTANCE) {
            ci.cancel(); // Cancela a renderização
            return;
        }

        // Culling baseado no campo de visão
        if (ParticleOptimizer.isOutsideFieldOfView(particlePos, camera)) {
            ci.cancel(); // Cancela a renderização
            return;
        }
        
        // Se chegou até aqui, a partícula deve ser renderizada.
        // Calcule e armazene o LOD para esta partícula (será usado no ticking).
        ParticleOptimizer.getParticleLOD((Particle)(Object)this, camera);
    }

    // Injeta no início do método markDead() da partícula
    @Inject(method = "markDead", at = @At("HEAD"))
    private void barium$onMarkDead(CallbackInfo ci) {
        // Quando uma partícula é marcada como morta, remova-a dos nossos mapas de otimização
        ParticleOptimizer.removeParticle((Particle) (Object) this);
    }
}