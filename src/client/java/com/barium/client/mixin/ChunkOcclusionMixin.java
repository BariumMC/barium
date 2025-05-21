package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOcclusionOptimizer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para integrar o otimizador de occlusion culling avançado
 */
@Mixin(ChunkBuilder.BuiltChunk.class)
public class ChunkOcclusionMixin {
    
    /**
     * Injeta no método de verificação de visibilidade do chunk para aplicar occlusion culling avançado
     */
    @Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
    private void onShouldBuild(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        // Verifica se o chunk deve ser renderizado com base no occlusion culling avançado
        ChunkBuilder.BuiltChunk chunk = (ChunkBuilder.BuiltChunk)(Object)this;
        if (!ChunkOcclusionOptimizer.shouldRenderChunkSection(chunk, camera)) {
            cir.setReturnValue(false); // Não renderiza este chunk
        }
    }
}
