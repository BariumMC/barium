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
import net.minecraft.client.render.VertexConsumer; // Necessário para o contexto do redirect
import net.minecraft.client.render.VertexConsumerProvider; // Necessário para o contexto do redirect
import net.minecraft.client.util.math.MatrixStack; // Necessário para o contexto do redirect
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.particle.ParticleTextureSheet; // Necessário para o loop do iterador
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator; // Necessário para manipular o iterador
import java.util.Map; // Necessário para o mapa de partículas
import java.util.Queue; // Necessário para a fila de partículas

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow protected abstract <T extends Particle> T addParticle(T particle);
    @Shadow private ClientWorld world;
    @Shadow protected abstract void tickParticle(Particle particle);

    // Shadow para acessar o mapa de partículas por tipo de textura.
    // É crucial para o loop de renderização.
    @Shadow private Map<ParticleTextureSheet, Queue<Particle>> particlesByType;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void barium$onInit(ClientWorld world, CallbackInfo ci) {
        ParticleTracker.resetParticleCount();
    }

    @Inject(
            method = "addParticle(Lnet/minecraft/client/particle/Particle;)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void barium$onAddParticle(Particle particle, CallbackInfoReturnable<Particle> cir) {
        if (ParticleTracker.isParticleLimitExceeded()) {
            if (BariumConfig.ENABLE_DEBUG_LOGGING) {
                BariumMod.LOGGER.debug("Denied adding particle due to limit: " + ParticleTracker.getCurrentParticleCount() + "/" + BariumConfig.MAX_PARTICLES_TOTAL);
            }
            cir.setReturnValue(null);
            return;
        }
        ParticleTracker.incrementParticleCount();
    }

    @Redirect(
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V")
    )
    private void barium$redirectParticleTick(Particle instance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null || client.gameRenderer.getCamera() == null) {
            instance.tick();
            return;
        }

        Camera camera = client.gameRenderer.getCamera();

        if (ParticleOptimizer.shouldTickParticle(instance, camera)) {
            instance.tick();
        } else {
            // Se shouldTickParticle retornar false, o tick é pulado.
            // Se a razão for culling por distância, a ParticleOptimizer já gerencia a chamada a expire().
        }
    }

    // --- NOVA LÓGICA DE CULLING DE RENDERIZAÇÃO ---
    // Esta injeção redefine o comportamento do loop de renderização.
    // Vamos interceptar a chamada a `Queue.iterator()` e retornar um iterador customizado
    // que filtra as partículas.
    @Redirect(
            method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client.render/VertexConsumerProvider;Lnet.minecraft.client.render/LightmapTextureManager;Lnet.minecraft.client.render.Camera;F)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;")
    )
    private Iterator<Particle> barium$redirectQueueIterator(
            Queue<Particle> queue,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, LightmapTextureManager lightmapTextureManager,
            Camera camera, float tickDelta
    ) {
        // Retorna um iterador que filtra as partículas.
        // Este iterador vai encapsular o iterador original da fila.
        return new FilteredParticleIterator(queue.iterator(), camera);
    }

    /**
     * Iterador customizado que filtra partículas para renderização.
     */
    private static class FilteredParticleIterator implements Iterator<Particle> {
        private final Iterator<Particle> originalIterator;
        private final Camera camera;
        private Particle nextParticle; // A próxima partícula a ser retornada

        public FilteredParticleIterator(Iterator<Particle> originalIterator, Camera camera) {
            this.originalIterator = originalIterator;
            this.camera = camera;
            findNextValidParticle(); // Encontra a primeira partícula válida
        }

        // Tenta encontrar a próxima partícula que deve ser renderizada.
        private void findNextValidParticle() {
            nextParticle = null; // Reseta
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
            return nextParticle != null; // Se há uma partícula válida, tem próximo
        }

        @Override
        public Particle next() {
            Particle current = nextParticle;
            findNextValidParticle(); // Prepara a próxima partícula para a próxima chamada a next()
            return current;
        }

        @Override
        public void remove() {
            // Opcional: implementar se o método remove() do iterador for usado, o que é raro para loops for-each.
            // originalIterator.remove();
            throw new UnsupportedOperationException("Remove not supported by this iterator.");
        }
    }
}