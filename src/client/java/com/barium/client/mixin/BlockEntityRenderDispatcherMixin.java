// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/BlockEntityRenderDispatcherMixin.java ---
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow private Camera camera;

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends BlockEntity> void barium$advancedBlockEntityCulling(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (this.camera == null) {
            return;
        }
        
        // Estágio 1: Culling por distância (configurável)
        // Esta é a verificação mais rápida, então é feita primeiro.
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
             if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, this.camera)) {
                ci.cancel();
                return;
            }
        }
       
        // Estágio 2: Culling por oclusão (raycast, mais caro, configurável)
        // Só é executado se o estágio 1 passar.
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            if (ChunkOptimizer.isBlockEntityOccluded(blockEntity, this.camera)) {
                ci.cancel();
            }
        }
    }
}