package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
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

    @Shadow
    private Camera camera;

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends BlockEntity> void barium$advancedBlockEntityCulling(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
    if (this.camera == null) {
        return;
    }

    // Verificação 1: A entidade está longe demais? (Otimização de distância)
    if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, this.camera)) {
        ci.cancel();
        return; // Retorna para não fazer a segunda verificação
    }

    // Verificação 2: A entidade está escondida atrás de um muro? (Otimização de oclusão)
    // Agora que está consertada, podemos usá-la com segurança.
    if (ChunkOptimizer.isBlockEntityOccluded(blockEntity, this.camera)) {
        ci.cancel();
    }
}