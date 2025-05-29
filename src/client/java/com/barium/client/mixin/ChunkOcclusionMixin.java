package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOcclusionOptimizer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box; // Necessário para criar Box
import net.minecraft.util.math.BlockPos; // Necessário para getOrigin().add(16,16,16)
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin para WorldRenderer para integrar o otimizador de occlusion culling avançado.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 * Nota: O ponto de injeção exato para occlusion culling pode variar e precisar de ajustes.
 * Esta implementação tenta redirecionar uma verificação de visibilidade.
 * Corrigido: Alvo do Redirect para `ChunkBuilder.BuiltChunk.shouldNotCull` para melhor acesso ao chunk.
 * Corrigido: Uso de getOrigin() para obter o Bounding Box do BuiltChunk.
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
        // O método original `shouldNotCull` internamente chama `frustum.isVisible(this.boundingBox)`.
        // Capturamos aqui, então `builtChunk` é a instância de `BuiltChunk` sendo processada.

        // Primeiro, executa a verificação original do frustum
        // builtChunk.getOrigin() retorna a BlockPos do canto (0,0,0) da seção do chunk.
        // Uma seção de chunk tem 16x16x16 blocos.
        BlockPos origin = builtChunk.getOrigin();
        Box builtChunkBox = new Box(origin, origin.add(16, 16, 16));

        boolean vanillaFrustumVisible = frustum.isVisible(builtChunkBox);
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