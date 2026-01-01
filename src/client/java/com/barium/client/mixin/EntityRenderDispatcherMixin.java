package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRenderManager.class, priority = 2000)
public class EntityRenderDispacherMixin {

    /**
     * Culling de entidades por distância.
     * Executa ANTES de qualquer renderer.
     * Mesmo ponto usado por Sodium/Iris.
     */
    @Inject(
        method = "shouldRender",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends Entity> void barium$distanceCulling(
            E entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!BariumConfig.C.ENABLE_ENTITY_CULLING) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.cameraEntity == null) {
            return;
        }

        // Distância ao quadrado (mais rápido, sem sqrt)
        double dx = client.cameraEntity.getX() - entity.getX();
        double dy = client.cameraEntity.getY() - entity.getY();
        double dz = client.cameraEntity.getZ() - entity.getZ();

        double distanceSq = dx * dx + dy * dy + dz * dz;

        // Exemplo: 64 blocos (ajustável por config)
        double maxDistSq = BariumConfig.C.ENTITY_CULL_DISTANCE * BariumConfig.C.ENTITY_CULL_DISTANCE;

        if (distanceSq > maxDistSq) {
            cir.setReturnValue(false);
        }
    }
}
