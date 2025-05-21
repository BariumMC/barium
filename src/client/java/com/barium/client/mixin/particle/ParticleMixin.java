package com.barium.client.mixin.particle;

// As importações Camera, VertexConsumer, Vec3d não são mais necessárias aqui, pois a lógica de renderização e posição
// foi movida para ParticleManagerMixin e ParticleOptimizer.
import com.barium.client.optimization.ParticleOptimizer; // Apenas para removeParticle()
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleAccessor {

    // REMOVIDO: A injeção no método render() foi movida para ParticleManagerMixin.
    // REMOVIDO: A injeção no método tick() foi removida conforme sua solicitação.

    // Injeta no início do método markDead() da partícula
    // Este método é chamado quando a partícula é removida (e.g., vida útil acaba, explode, etc.).
    @Inject(method = "markDead", at = @At("HEAD"))
    private void barium$onMarkDead(CallbackInfo ci) {
        // Quando uma partícula é marcada como morta, remova-a dos nossos mapas de otimização
        // para liberar memória e evitar referências a objetos que não existem mais.
        ParticleOptimizer.removeParticle((Particle) (Object) this);
    }
}