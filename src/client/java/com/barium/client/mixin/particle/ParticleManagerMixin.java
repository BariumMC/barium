package com.barium.client.mixin.particle;

import com.barium.BariumMod;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ParticleTracker;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    // Adicione o descritor explícito para addParticle e tickParticle para clareza
    @Shadow protected abstract <T extends Particle> T addParticle(T particle);
    @Shadow private ClientWorld world;
    @Shadow protected abstract void tickParticle(Particle particle);
    @Shadow private Map<ParticleTextureSheet, Queue<Particle>> particlesByType;

    private final Random random = new Random();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void barium$onInit(ClientWorld world, CallbackInfo ci) {
        if (BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            ParticleTracker.resetParticleCount();
        }
    }

    /**
     * Intercepta a adição de novas partículas para aplicar o limite dinâmico e redução de emissão.
     */
    @Inject(
            method = "addParticle(Lnet/minecraft/client/particle/Particle;)Lnet/minecraft/client/particle/Particle;", // Descritor já parece correto aqui
            at = @At("HEAD"),
            cancellable = true
    )
    private void barium$onAddParticle(Particle particle, CallbackInfoReturnable<Particle> cir) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            return;
        }

        if (BariumConfig.REDUCE_PARTICLE_EMISSION && random.nextFloat() < 0.5f) {
            if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Denied adding particle due to emission reduction.");
            }
            cir.setReturnValue(null);
            return;
        }

        if (ParticleTracker.isParticleLimitExceeded()) {
            if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Denied adding particle due to limit: " + ParticleTracker.getCurrentParticleCount() + "/" + BariumConfig.MAX_TOTAL_PARTICLES);
            }
            cir.setReturnValue(null);
            return;
        }
        ParticleTracker.incrementParticleCount();
    }

    /**
     * Redireciona a chamada ao método tick() de cada partícula dentro do loop principal de tick do ParticleManager.
     */
    @Redirect(
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V")
    )
    private void barium$redirectParticleTick(Particle instance) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            instance.tick();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            instance.tick();
            return;
        }

        Camera camera = client.gameRenderer.getCamera();

        if (ParticleOptimizer.shouldTickParticle(instance, camera)) {
            instance.tick();
        } else {
            if (BariumConfig.ENABLE_PARTICLE_CULLING &&
                ParticleOptimizer.getParticlePosition(instance).squaredDistanceTo(camera.getPos()) > BariumConfig.PARTICLE_CULLING_DISTANCE_SQ) {
                instance.expire();
            }
        }
    }

    /**
     * NOVA LÓGICA DE CULLING DE RENDERIZAÇÃO:
     * Intercepta a chamada a `Queue.iterator()` dentro do loop de renderização do ParticleManager
     * para filtrar partículas que não devem ser renderizadas.
     */
    @Redirect(
            method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client.render/Camera;F)V", // <--- LINHA 121 ORIGINALMENTE
            // CORRIGIDO: Lnet.minecraft.client.render/ -> Lnet/minecraft/client/render/
            // AQUI ESTAVA O ERRO PRINCIPAL!
            at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;")
    )
    private Iterator<Particle> barium$redirectQueueIterator(
            Queue<Particle> queue,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, LightmapTextureManager lightmapTextureManager,
            Camera camera, float tickDelta
    ) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS || !BariumConfig.ENABLE_PARTICLE_CULLING) {
            return queue.iterator();
        }
        return new FilteredParticleIterator(queue.iterator(), camera);
    }

    /**
     * Iterador customizado que filtra partículas para renderização.
     */
    private static class FilteredParticleIterator implements Iterator<Particle> {
        private final Iterator<Particle> originalIterator;
        private final Camera camera;
        private Particle nextParticle;

        public FilteredParticleIterator(Iterator<Particle> originalIterator, Camera camera) {
            this.originalIterator = originalIterator;
            this.camera = camera;
            findNextValidParticle();
        }

        private void findNextValidParticle() {
            nextParticle = null;
            while (originalIterator.hasNext()) {
                Particle particle = originalIterator.next();
                if (ParticleOptimizer.shouldRenderParticle(particle, camera)) {
                    nextParticle = particle;
                    return;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return nextParticle != null;
        }

        @Override
        public Particle next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            Particle current = nextParticle;
            findNextValidParticle();
            return current;
        }

        @Override
        public void remove() {
            originalIterator.remove();
        }
    }
}