// ARQUIVO RENOMEADO E CORRIGIDO: src/client/java/com/barium/client/mixin/BlockEntityRenderManagerMixin.java
package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
// CORREÇÃO: Import da classe renomeada para BlockEntityRenderManager.
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// CORREÇÃO: O Mixin agora aponta para a classe correta: BlockEntityRenderManager
@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityRenderManagerMixin {

    @Shadow private Camera camera;

    @Inject(
        // A assinatura do método "render" continua a mesma, então não precisamos mudar aqui.
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends BlockEntity> void barium$advancedBlockEntityCulling(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        // A lógica de culling permanece a mesma, pois continua válida.
        if (this.camera == null) {
            return;
        }
        
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
             if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, this.camera)) {
                ci.cancel();
                return;
            }
        }
       
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            if (ChunkOptimizer.isBlockEntityOccluded(blockEntity, this.camera)) {
                ci.cancel();
            }
        }
    }
}