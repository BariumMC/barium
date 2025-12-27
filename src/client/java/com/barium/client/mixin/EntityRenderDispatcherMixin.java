package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void barium$cullDistantShadows(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_CULLING) return;

        // Sombras são caras (geometria extra + mistura de transparência).
        // Remove sombras se a entidade estiver a mais de 16 blocos.
        double distSq = matrices.peek().getPositionMatrix().determinant(); // Aproximação de escala/distância ou usar entity.distanceTo(player)
        // Simplificação: usar a distância da câmera do MinecraftClient
        double camDistSq = net.minecraft.client.MinecraftClient.getInstance().gameRenderer.getCamera().getPos().squaredDistanceTo(entity.getPos());

        if (camDistSq > 256.0) { // 16 blocos
            ci.cancel();
        }
    }
    
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void barium$disableHitboxes(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float tickDelta, CallbackInfo ci) {
        // Hitboxes (F3+B) geram muitas linhas (draw calls). Desativa se não for dev.
        if (!net.minecraft.client.MinecraftClient.getInstance().options.debugEnabled) {
             ci.cancel();
        }
    }
}