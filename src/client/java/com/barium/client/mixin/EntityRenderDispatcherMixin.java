package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderManager; // Classe renomeada na 1.21.9
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderManager.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void barium$cullDistantShadows(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ENTITY_CULLING) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) return;

        // Usa as coordenadas da câmera
        double camX = client.gameRenderer.getCamera().getPos().x;
        double camY = client.gameRenderer.getCamera().getPos().y;
        double camZ = client.gameRenderer.getCamera().getPos().z;

        // Usa getters de coordenadas primitivos (seguro para todas as versões)
        double entX = entity.getX();
        double entY = entity.getY();
        double entZ = entity.getZ();

        double dx = camX - entX;
        double dy = camY - entY;
        double dz = camZ - entZ;
        
        double camDistSq = dx * dx + dy * dy + dz * dz;

        // Se estiver a mais de 16 blocos (256 unidades quadradas), não desenha a sombra
        if (camDistSq > 256.0) { 
            ci.cancel();
        }
    }
    
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void barium$disableHitboxes(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float tickDelta, CallbackInfo ci) {
        // Se a opção de desativar outlines estiver ativa, desativamos hitboxes também
        if (BariumConfig.C.DISABLE_ENTITY_OUTLINES) {
             ci.cancel();
        }
    }
}