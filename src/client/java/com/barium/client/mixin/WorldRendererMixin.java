package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin Focado e Estável
 * 
 * Neste momento, vamos focar em tomar o controle do AGENDAMENTO de rebuilds,
 * que é um passo estável e garantido de funcionar. A tomada da renderização
 * será o próximo passo, uma vez que tenhamos uma base compilável.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Nullable private ClientWorld world;

    // --- Injeções de setup e schedule (ESTÁVEIS) ---

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        // Inicializa/limpa nosso manager
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    @Inject(method = "setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (this.world == null || this.client.player == null) return;
        if (BariumConfig.C.ENABLE_FRUSTUM_CHUNK_CULLING) ChunkRenderManager.getInstance().calculateChunksToRender(this.client, frustum);
        if (BariumConfig.C.ENABLE_FLOOD_FILL_CULLING) FloodFillVisibilityManager.getInstance().update(this.client);
        if (BariumConfig.C.ENABLE_VISIBILITY_GRAPH_CULLING) ChunkVisibilityManager.getInstance().update(this.client);
    }

    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        ChunkUploadThrottler.resetCounter();
    }

    /**
     * Esta é a tomada de controle mais importante e estável.
     * Nós interceptamos o pedido para reconstruir um chunk, passamos para nosso sistema,
     * e impedimos o Minecraft de fazer o trabalho. Isso VAI funcionar.
     */
    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel();
    }
    
    //
    // A INJEÇÃO DE RENDERIZAÇÃO FOI REMOVIDA TEMPORARIAMENTE
    //
    // Vamos primeiro garantir que o resto compile. Uma vez que o agendamento
    // esteja sob nosso controle, podemos implementar o meshing e o upload,
    // e então, como passo final, encontrar o ponto exato para injetar e desenhar.
    //
}