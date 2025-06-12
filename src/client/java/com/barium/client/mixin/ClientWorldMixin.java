// --- Crie este novo arquivo em: src/client/java/com/barium/client/mixin/ClientWorldMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    /**
     * Injeta no início do método que atualiza a lógica de cada entidade no lado do cliente.
     * Se a otimização estiver ativa e a entidade estiver longe, sua lógica será
     * atualizada com menos frequência, economizando CPU.
     */
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void barium$cullDistantEntityTicks(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;

        // Não otimizar o jogador, sua montaria ou entidades com passageiros
        if (entity.isPlayer() || entity.hasPassengers() || entity.getVehicle() != null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Calcula a distância ao quadrado (muito mais rápido que a raiz quadrada)
        double distanceSq = entity.getPos().squaredDistanceTo(client.player.getPos());

        // Se a entidade estiver além da distância configurada...
        if (distanceSq > BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ) {
            // ...só atualize sua lógica a cada 4 ticks (redução de 75% no custo)
            if (entity.age % 4 != 0) {
                ci.cancel(); // Pula o resto do método de tick da entidade
            }
        }
    }

    /**
     * Injeta no início do método que gera partículas de ambiente (fumaça de tocha, etc).
     * Se a otimização estiver ativa, este método será executado apenas na metade das vezes,
     * cortando seu custo de CPU pela metade.
     */
    @Inject(method = "animateTicks", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int x, int y, int z, CallbackInfo ci) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;
        
        // Pula a execução deste método em ticks pares.
        // A conversão (World)(Object)this é necessária para acessar o tempo do mundo.
        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel();
        }
    }
}