package com.barium.client.mixin.particle;

import com.barium.client.optimization.ParticleOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d; // Necessário para operar com Vec3d

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    /**
     * Redireciona a chamada ao método render() de cada partícula dentro do ParticleManager.renderParticles().
     * Isso nos permite aplicar culling e LOD de renderização antes que a partícula seja desenhada.
     *
     * @param instance O objeto Particle no qual o render() seria chamado.
     * @param vertexConsumer O VertexConsumer para onde a geometria da partícula é construída.
     * @param camera A câmera atual do jogador.
     * @param tickDelta O delta do tick.
     */
    @Redirect(
        method = "renderParticles", // Método em ParticleManager que renderiza as partículas
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;render(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"
        )
    )
    private void barium$redirectParticleRender(Particle instance, VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        // SEMPRE calcule o LOD para esta partícula se a otimização de LOD estiver habilitada.
        // Isso é feito aqui porque o LOD afeta a frequência de ticking, mesmo que a partícula seja culled da renderização.
        if (BariumConfig.ENABLE_PARTICLE_LOD) {
            ParticleOptimizer.getParticleLOD(instance, camera);
        }

        // Aplica o culling de renderização se habilitado
        if (BariumConfig.ENABLE_PARTICLE_CULLING) {
            Vec3d particlePos = ParticleOptimizer.getParticlePosition(instance); // Agora public
            Vec3d cameraPos = camera.getPos();
            double distance = particlePos.distanceTo(cameraPos);

            // Se a partícula estiver fora da distância de culling ou fora do FOV, não a renderize.
            if (distance > BariumConfig.PARTICLE_CULLING_DISTANCE ||
                ParticleOptimizer.isOutsideFieldOfView(particlePos, camera)) {
                return; // Pula a chamada ao método render() original
            }
        }
        
        // Se a partícula não foi culled (ou culling está desabilitado), chame o método render() original.
        instance.render(vertexConsumer, camera, tickDelta);
    }
}