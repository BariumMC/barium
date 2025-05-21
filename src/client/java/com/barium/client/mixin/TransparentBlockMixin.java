package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

/**
 * Mixin para integrar o otimizador de renderização de blocos transparentes
 */
@Mixin(ChunkBuilder.BuiltChunk.class)
public class TransparentBlockMixin {
    
    /**
     * Injeta no método de renderização de blocos transparentes para aplicar sorting avançado
     */
    @Inject(
        method = "rebuildChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;renderLayer(Lnet/minecraft/client/render/RenderLayer;)V"),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void onRenderTransparentBlocks(ChunkRendererRegion region, CallbackInfo ci) {
        // Prepara os blocos transparentes com sorting otimizado
        ChunkBuilder.BuiltChunk chunk = (ChunkBuilder.BuiltChunk)(Object)this;
        List<BlockPos> sortedBlocks = TransparentBlockOptimizer.prepareTransparentBlocks(chunk, region);
        
        if (sortedBlocks != null && !sortedBlocks.isEmpty()) {
            // Agrupa os blocos por tipo para otimizar draw calls
            Map<BlockState, List<BlockPos>> groupedBlocks = 
                    TransparentBlockOptimizer.groupTransparentBlocksByType(sortedBlocks, region);
            
            // Otimiza a ordem de renderização
            TransparentBlockOptimizer.optimizeRenderOrder(groupedBlocks);
        }
    }
}
