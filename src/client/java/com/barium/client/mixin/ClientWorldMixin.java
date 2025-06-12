// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/ClientWorldMixin.java ---
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

    // Este mixin já estava correto, nenhuma mudança necessária aqui.
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void barium$cullDistantEntityTicks(Entity entity, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return;
        if (entity.isPlayer() || entity.hasPassengers() || entity.getVehicle() != null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        double distanceSq = entity.getPos().squaredDistanceTo(client.player.getPos());
        if (distanceSq > BariumConfig.C.ENTITY_TICK_CULLING_DISTANCE_SQ) {
            if (entity.age % 4 != 0) {
                ci.cancel();
            }
        }
    }

    /**
     * Injeta no início do método que gera partículas de ambiente (fumaça de tocha, etc).
     * Se a otimização estiver ativa, este método será executado apenas na metade das vezes,
     * cortando seu custo de CPU pela metade.
     */
    // CORREÇÃO: Adicionamos a assinatura do método (III)V para remover o aviso de compilação.
    // (III)V significa: um método que recebe 3 Inteiros (int) e retorna Void (void).
    @Inject(method = "animateTicks(III)V", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int x, int y, int z, CallbackInfo ci) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;
        
        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel();
        }
    }
}