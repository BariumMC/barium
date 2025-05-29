package com.barium.client.mixin;

import com.barium.client.optimization.AdvancedOcclusionCulling;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack; // Import correto para MatrixStack
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para WorldRenderer para integrar o sistema avançado de occlusion culling (ex: HZB).
 * Modifica a lógica de determinação de visibilidade dos chunks.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 * Corrigido import de MatrixStack e assinatura do método render.
 * Corrigido: Alvo do Redirect para `ChunkBuilder.BuiltChunk.shouldNotCull` para melhor acesso ao chunk.
 */
@Mixin(WorldRenderer.class)
public abstract class AdvancedOcclusionCullingMixin {

    /**
     * Redireciona a verificação de visibilidade do frustum para incluir a verificação de oclusão avançada.
     *
     * Target Class: net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk
     * Target Method: shouldNotCull(Lnet/minecraft/client/render/Frustum;)Z
     * Isso permite acesso direto à instância BuiltChunk (`this` do método original) e ao argumento Frustum.
     */
    @Redirect(
        method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;shouldNotCull(Lnet/minecraft/client/render/Frustum;)Z"
        )
    )
    private boolean barium$advancedOcclusionCheckRedirect(ChunkBuilder.BuiltChunk builtChunk, Frustum frustum, Camera camera, Frustum frustum_original, boolean hasForcedFrustum, boolean spectator) {
        // O método original `shouldNotCull` internamente chama `frustum.isVisible(chunk.getBounds())`.
        // Capturamos aqui, então `builtChunk` é a instância de `BuiltChunk` sendo processada.

        // 1. Verificação original do Frustum
        // A lógica do método `shouldNotCull` do Minecraft já faz isso. Se o nosso redirecionamento
        // retornar 'false' no final, a verificação original será efetivamente cancelada.
        // Para uma verificação explícita do frustum, podemos usar:
        boolean vanillaFrustumVisible = frustum.isVisible(builtChunk.getBounds());

        if (!vanillaFrustumVisible) {
            return false; // Se não está no frustum, está definitivamente ocluído.
        }

        // 2. Verificação de Oclusão Avançada (ex: HZB)
        // Agora temos acesso direto ao `builtChunk`
        if (AdvancedOcclusionCulling.isChunkOccluded(builtChunk, camera)) {
            return false; // Ocluído pelo sistema avançado.
        }

        // Se passou em ambas as verificações, é considerado visível.
        return true;
    }

    /**
     * Injeta no final do método de renderização principal para atualizar estruturas de dados de oclusão (ex: HZB).
     *
     * Target Class: net.minecraft.client.render.WorldRenderer
     * Target Method Signature (Yarn 1.21.5+build.1): render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V
     */
    @Inject(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At("RETURN") // Executa após a renderização do frame
    )
    private void barium$updateOcclusionData(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        // Atualiza as estruturas de dados de oclusão (ex: HZB) após o frame ser renderizado.
        AdvancedOcclusionCulling.updateOcclusionData(camera);
    }
}