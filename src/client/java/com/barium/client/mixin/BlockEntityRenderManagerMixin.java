package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityRenderManagerMixin {

    /**
     * Injeta no método `getRenderState` para aplicar o culling (remoção) de entidades de bloco.
     * Esta é a abordagem correta para o motor de renderização do Minecraft 1.21.10.
     * A assinatura do método agora usa tipos explícitos para garantir a compatibilidade e robustez do Mixin.
     */
    @Inject(
        method = "getRenderState(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)Lnet/minecraft/client/render/block/entity/state/BlockEntityRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$advancedBlockEntityCulling(
            BlockEntity blockEntity,
            float tickProgress,
            ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay,
            CallbackInfoReturnable<BlockEntityRenderState> cir) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) {
            return;
        }
        Camera camera = client.gameRenderer.getCamera();

        // Estágio 1: Culling por distância
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_CULLING) {
            if (!ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
                cir.setReturnValue(null); // Retorna null para impedir a criação do RenderState e pular a renderização.
                return;
            }
        }

        // Estágio 2: Culling por oclusão
        if (BariumConfig.C.ENABLE_BLOCK_ENTITY_OCCLUSION_CULLING) {
            if (ChunkOptimizer.isBlockEntityOccluded(blockEntity, camera)) {
                cir.setReturnValue(null); // Retorna null para pular a renderização.
            }
        }
    }
}