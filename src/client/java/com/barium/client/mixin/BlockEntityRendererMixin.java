package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Vec3d; // Importar Vec3d
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderer.class)
public abstract class BlockEntityRendererMixin<T extends BlockEntity> {

    // Reativando a injeção no método 'render' com a nova assinatura para 1.21.5
    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullBlockEntityRender(T entity, float tickProgress, MatrixStack matrices,
                                              VertexConsumerProvider vertexConsumers, int light, int overlay,
                                              Vec3d cameraPos, // O novo argumento cameraPos
                                              CallbackInfo ci) {
        // Obter a câmera para usar no ChunkOptimizer (ainda precisamos dela para o frustum culling, mesmo que desativado)
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        if (!ChunkOptimizer.shouldRenderBlockEntity(entity, camera)) {
            ci.cancel(); // Cancela a renderização se a entidade deve ser ocultada
        }
    }
}