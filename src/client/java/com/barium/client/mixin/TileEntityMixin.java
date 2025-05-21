package com.barium.client.mixin;

import com.barium.client.optimization.TileEntityOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mixin para integrar o otimizador de renderização de tile entities
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class TileEntityMixin {
    
    /**
     * Injeta no método de renderização de tile entities para aplicar otimizações
     */
    @Inject(
        method = "render(Ljava/util/Collection;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderTileEntities(Collection<BlockEntity> blockEntities, 
                                     MatrixStack matrices, 
                                     VertexConsumerProvider vertexConsumers, 
                                     CallbackInfo ci) {
        // Prepara as tile entities para renderização otimizada
        Map<Class<? extends BlockEntity>, List<BlockEntity>> groupedEntities = 
                TileEntityOptimizer.prepareForRendering(blockEntities);
        
        if (groupedEntities != null && !groupedEntities.isEmpty()) {
            // Renderiza cada grupo de tile entities com otimizações
            BlockEntityRenderDispatcher dispatcher = (BlockEntityRenderDispatcher)(Object)this;
            
            for (Map.Entry<Class<? extends BlockEntity>, List<BlockEntity>> entry : groupedEntities.entrySet()) {
                TileEntityOptimizer.renderEntitiesByType(
                    entry.getKey(),
                    entry.getValue(),
                    dispatcher,
                    matrices,
                    vertexConsumers,
                    dispatcher.camera.getLightLevel(),
                    0 // overlay
                );
            }
            
            // Cancela o método original, pois já renderizamos tudo
            ci.cancel();
        }
    }
}
