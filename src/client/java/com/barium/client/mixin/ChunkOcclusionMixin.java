package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOcclusionOptimizer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin para WorldRenderer para integrar o otimizador de occlusion culling avançado.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 * Nota: O ponto de injeção exato para occlusion culling pode variar e precisar de ajustes.
 * Esta implementação tenta redirecionar uma verificação de visibilidade.
 * Corrigido: Alvo do Redirect para `ChunkBuilder.BuiltChunk.shouldNotCull` para melhor acesso ao chunk.
 */
@Mixin(WorldRenderer.class)
public abstract class ChunkOcclusionMixin {

    /**
     * Redireciona a verificação de visibilidade do frustum para incluir a verificação de oclusão avançada.
     * Similar ao AdvancedOcclusionCullingMixin, mas para ChunkOcclusionOptimizer.
     *
     * Target Class: net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk
     * Target Method: shouldNotCull(Lnet/minecraft/client/render/Frustum;)Z
     * Isso permite acesso direto à instância BuiltChunk (`this` do método original) e ao argumento Frustum.
     */
    @Redirect(
        method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", // More likely target for visibility setup
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;shouldNotCull(Lnet/minecraft/client/render/Frustum;)Z"
        )
    )
    private boolean barium$advancedOcclusionCheck(ChunkBuilder.BuiltChunk builtChunk, Frustum frustum, Camera camera, Frustum frustum_original, boolean hasForcedFrustum, boolean spectator) {
        // O método original `shouldNotCull` internamente chama `frustum.isVisible(chunk.getBounds())`.
        // Capturamos aqui, então `builtChunk` é a instância de `BuiltChunk` sendo processada.

        // Primeiro, executa a verificação original do frustum
        boolean vanillaFrustumVisible = frustum.isVisible(builtChunk.getBounds());
        if (!vanillaFrustumVisible) {
            return false; // Se não está no frustum, definitivamente não é visível
        }

        // Se está no frustum, aplica a verificação de oclusão avançada
        // Agora temos acesso direto ao `builtChunk`
        if (!ChunkOcclusionOptimizer.shouldRenderChunkSection(builtChunk, camera)) {
             // O otimizador diz que está ocluído
             return false;
        }

        // Se passou no frustum e na nossa verificação de oclusão, é visível.
        return true;
    }
}