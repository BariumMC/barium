package com.barium.client.mixin.particle;

import com.barium.config.BariumConfig;
import com.barium.client.optimization.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer; // Alterado de VertexConsumerProvider para VertexConsumer
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleAccessor { // Implementa a interface Accessor

    // CORRIGIDO: Injeta no início do método buildGeometry() da partícula
    // Este é o método de renderização correto para 1.21.5
    @Inject(method = "buildGeometry", at = @At("HEAD"), cancellable = true)
    private void barium$beforeBuildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta, CallbackInfo ci) {
        // Se o culling estiver desabilitado, não faz nada e permite a renderização
        if (!BariumConfig.ENABLE_PARTICLE_CULLING) {
            // Mesmo sem culling, calcule o LOD para que o ticking possa ser otimizado
            ParticleOptimizer.getParticleLOD((Particle)(Object)this, camera);
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

    // Injeta no início do método tick() da partícula para controlar sua atualização
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void barium$beforeTick(CallbackInfo ci) {
        // Se ParticleOptimizer.shouldTickParticle retornar false, cancela o método tick()
        // Isso impede que a lógica de atualização da partícula seja executada para este tick.
        if (!ParticleOptimizer.shouldTickParticle((Particle)(Object)this)) {
            ci.cancel(); 
        }
    }

    // Injeta no início do método markDead() da partícula
    @Inject(method = "markDead", at = @At("HEAD"))
    private void barium$onMarkDead(CallbackInfo ci) {
        // Quando uma partícula é marcada como morta, remova-a dos nossos mapas de otimização
        ParticleOptimizer.removeParticle((Particle) (Object) this);
    }
}