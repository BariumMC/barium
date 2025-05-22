package com.barium.client.mixin.particle;

package com.barium.mixin;

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
import java.util.Random; // Para REDUCE_PARTICLE_EMISSION

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow protected abstract <T extends Particle> T addParticle(T particle);
    @Shadow private ClientWorld world;
    @Shadow protected abstract void tickParticle(Particle particle);
    @Shadow private Map<ParticleTextureSheet, Queue<Particle>> particlesByType;

    private final Random random = new Random(); // Para REDUCE_PARTICLE_EMISSION

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
            method = "addParticle(Lnet/minecraft/client/particle/Particle;)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void barium$onAddParticle(Particle particle, CallbackInfoReturnable<Particle> cir) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            return; // Permite a adição normal se o mod está desabilitado
        }

        // Reduz a emissão de partículas se configurado
        if (BariumConfig.REDUCE_PARTICLE_EMISSION && random.nextFloat() < 0.5f) { // Ex: 50% de chance de não emitir
            if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Denied adding particle due to emission reduction.");
            }
            cir.setReturnValue(null);
            return;
        }

        // Aplica o limite dinâmico
        if (ParticleTracker.isParticleLimitExceeded()) {
            if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Denied adding particle due to limit: " + ParticleTracker.getCurrentParticleCount() + "/" + BariumConfig.MAX_TOTAL_PARTICLES);
            }
            cir.setReturnValue(null); // Impede a adição da partícula
            return;
        }
        ParticleTracker.incrementParticleCount(); // Permite a adição, então incrementa a contagem
    }

    /**
     * Redireciona a chamada ao método tick() de cada partícula dentro do loop principal de tick do ParticleManager.
     * Isso nos permite aplicar culling por distância/frustum e LOD para o tick da partícula.
     */
    @Redirect(
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V")
    )
    private void barium$redirectParticleTick(Particle instance) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS) {
            instance.tick(); // Permite o tick normal se o mod está desabilitado
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            instance.tick();
            return;
        }

        Camera camera = client.gameRenderer.getCamera();

        // shouldTickParticle já lida com culling por distância (se deve expirar) e LOD de tick.
        if (ParticleOptimizer.shouldTickParticle(instance, camera)) {
            instance.tick(); // Chama o tick original se a otimização permitir
        } else {
            // Se shouldTickParticle retornou false, significa que a partícula deve ser expirada
            // (devido à distância de culling) ou seu tick foi pulado (devido ao LOD).
            // A ParticleManager irá remover partículas expiradas no próximo ciclo.
            if (BariumConfig.ENABLE_PARTICLE_CULLING &&
                ParticleOptimizer.getParticlePosition(instance).squaredDistanceTo(camera.getPos()) > BariumConfig.PARTICLE_CULLING_DISTANCE_SQ) {
                instance.expire(); // Garante que partículas muito distantes expirem para serem removidas.
            }
        }
    }

    /**
     * NOVA LÓGICA DE CULLING DE RENDERIZAÇÃO:
     * Intercepta a chamada a `Queue.iterator()` dentro do loop de renderização do ParticleManager
     * para filtrar partículas que não devem ser renderizadas.
     */
    @Redirect(
            method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client.render/VertexConsumerProvider;Lnet.minecraft.client.render/LightmapTextureManager;Lnet.minecraft.client.render.Camera;F)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;")
    )
    private Iterator<Particle> barium$redirectQueueIterator(
            Queue<Particle> queue,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, LightmapTextureManager lightmapTextureManager,
            Camera camera, float tickDelta
    ) {
        if (!BariumConfig.ENABLE_MOD_OPTIMIZATIONS || !BariumConfig.ENABLE_PARTICLE_CULLING) {
            return queue.iterator(); // Retorna o iterador original se o mod ou culling estão desabilitados
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
            originalIterator.remove(); // Permite remover do iterador original se necessário
        }
    }
}