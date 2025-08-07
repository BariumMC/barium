// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/EntityRendererMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(
        method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullByDistance(T entity, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Boolean> cir) {
        // Se a otimização de distância estiver desligada, não fazemos nada.
        if (!BariumConfig.C.ENABLE_ENTITY_CULLING) {
            return;
        }

        // Delega a lógica de culling APENAS por distância para nossa classe.
        // Se a entidade estiver muito longe, cancelamos a renderização.
        // Se estiver perto, deixamos o Minecraft continuar e fazer seu próprio frustum culling.
        if (!EntityOptimizer.shouldRenderByDistance(entity, cameraX, cameraY, cameraZ)) {
            cir.setReturnValue(false);
        }
    }
}