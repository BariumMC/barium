package com.barium.client.mixin;

import com.barium.client.optimization.TileEntityOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BufferBuilderStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
// import java.util.Queue; // Não é mais Queue, mas sim List

@Mixin(WorldRenderer.class)
public abstract class TileEntityMixin {

    @Shadow private BufferBuilderStorage bufferBuilders;
    @Shadow private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Inject(
        method = "renderBlockEntities"
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void barium$optimizeBlockEntityRendering(
            MatrixStack matrices,
            VertexConsumerProvider.Immediate vertexConsumers,
            Camera camera,
            float tickDelta,
            CallbackInfo ci,
            List<BlockEntity> list // Ajustado para List, nome comum 'list'
    ) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // Chama o otimizador para processar e renderizar as entidades de forma otimizada
        // Você precisará implementar TileEntityOptimizer.processVisibleBlockEntities para lidar com isso
        if (TileEntityOptimizer.processVisibleBlockEntities(list, this.blockEntityRenderDispatcher, matrices, vertexConsumers, camera, tickDelta)) {
            ci.cancel(); // Se o otimizador cuidou da renderização, cancela a vanilla
        }
    }
}