package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin de Tomada de Controle Total para o WorldRenderer.
 * 
 * Este Mixin é o ponto central que permite que o Barium substitua completamente
 * o pipeline de renderização de chunks do Minecraft, semelhante ao Sodium.
 * 
 * Ele desativa a construção e o desenho de chunks vanilla e redireciona
 * esses processos para o nosso BariumRenderManager.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable ClientWorld world;
    @Shadow private ChunkBuilder chunkBuilder;

    // --- PARTE 1: GANCHOS DE CICLO DE VIDA E ATUALIZAÇÃO ---

    /**
     * Hook para quando o mundo é mudado (entrar/sair de um servidor/singleplayer).
     * Notifica nosso renderizador para limpar todos os dados de chunks, VBOs, etc.,
     * prevenindo memory leaks e preparando para o novo mundo.
     */
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    /**
     * Ponto de entrada unificado para atualizar todos os nossos managers de culling (Frustum, Flood-Fill, etc).
     * Injeta em `setupTerrain` para ter acesso ao Frustum e um ponto de atualização confiável por frame.
     */
    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (this.world == null || this.client.player == null) {
            return; // O onSetWorld já limpou tudo
        }

        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) {
            ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        }
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) {
            FloodFillVisibilityManager.getInstance().update(this.client);
        }
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) {
            ChunkVisibilityManager.getInstance().update(this.client);
        }
    }

    /**
     * Prepara para a fase de atualização de chunks.
     * Reseta o limitador de uploads por frame para garantir uma contagem limpa.
     */
    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        ChunkUploadThrottler.resetCounter();
    }


    // --- PARTE 2: TOMADA DE CONTROLE DO PROCESSO DE REBUILD ---

    /**
     * Intercepta TODAS as chamadas para agendar a reconstrução de um chunk.
     * Ao cancelar este método, impedimos o `ChunkBuilder` vanilla de fazer
     * qualquer trabalho de "meshing". Em vez disso, passamos o pedido para
     * o nosso próprio sistema de renderização.
     */
    @Inject(method = "scheduleRebuild", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        // Notifica nosso sistema que este chunk precisa ser reconstruído.
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);

        // Cancela a chamada vanilla. O ChunkBuilder original não fará mais NADA.
        // Isso economiza uma quantidade enorme de CPU.
        ci.cancel();
    }


    // --- PARTE 3: TOMADA DE CONTROLE DO PROCESSO DE RENDERIZAÇÃO ---

    /**
     * O GANCHO MAIS IMPORTANTE.
     * Redireciona a chamada de desenho final de cada RenderLayer.
     * No loop de `renderLayer`, o Minecraft configura o estado do OpenGL para o layer,
     * e então chama `layer.draw(...)` para desenhar os vértices.
     * 
     * Nós interceptamos essa chamada `draw`. Ao não chamar o método original, nós
     * efetivamente SILENCIAMOS a renderização de chunks do Minecraft.
     * No lugar, chamamos o nosso `BariumRenderManager`, que usará nossos VBOs,
     * nosso formato de vértice e nossa lógica de desenho otimizada.
     */
    @Redirect(
        method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/RenderLayer;draw(Lnet/minecraft/client/render/BufferBuilder;DDD)V"
        )
    )
    private void barium$redirectRenderLayerDraw(
            // Argumentos do método alvo (layer.draw) - NÓS OS IGNORAMOS
            RenderLayer layer, BufferBuilder buffer, double x, double y, double z,
            // Argumentos do método original (renderLayer) - NÓS OS USAMOS
            RenderLayer originalLayerArg, MatrixStack matrices, double cameraX, double cameraY, double cameraZ
    ) {
        // O corpo do redirect substitui a chamada inteira.
        // Como este corpo está vazio, a chamada `layer.draw` vanilla NUNCA ACONTECE.
        // ISSO DESLIGA A RENDERIZAÇÃO DE CHUNKS DO MINECRAFT.

        // Em seu lugar, chamamos o nosso próprio sistema para desenhar os chunks para este layer.
        BariumRenderManager.getInstance().renderLayer(matrices, originalLayerArg, cameraX, cameraY, cameraZ);
    }
}