// CONTEÚDO CORRIGIDO: src/client/java/com/barium/client/mixin/BlockEntityRenderManagerMixin.java
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient; // Import necessário
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityRenderManagerMixin {

    // CORREÇÃO: O @Shadow para o campo 'camera' foi removido, pois ele não existe mais nesta classe.

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends BlockEntity> void barium$advancedBlockEntityCulling(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        // CORREÇÃO: A câmera agora é obtida da forma moderna e segura.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) {
            return;
        }
        Camera camera = client.gameRenderer.getCamera();

        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
             if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
                ci.cancel();
                return;
            }
        }

        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            if (ChunkOptimizer.isBlockEntityOccluded(blockEntity, camera)) {
                ci.cancel();
            }
        }
    }
}