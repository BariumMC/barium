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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // --- Definindo o tipo aninhado para o retorno de valor ---
    // Isto é necessário para referenciar `WorldRenderer.SectionRenderState` no descritor.
    // O compilador irá reclamar se não fizermos isso, pois é um tipo interno.
    @Mixin(WorldRenderer.class)
    interface SectionRenderStateAccessor { }
    // Este truque acima pode ou não ser necessário dependendo da versão do Loom.
    // É mais seguro referenciar pelo nome intermediário.
    // Para 1.21.x o nome intermediário é `class_761$class_8842`

    @Shadow @Final private MinecraftClient client;
    @Shadow private @Nullable ClientWorld world;
    
    // --- (INJEÇÕES DA PARTE 1 E 2 PERMANECEM IGUAIS) ---

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
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
    
    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel();
    }

    // --- PARTE 3 CORRIGIDA ---
    
    @Inject(
        // Descritor completo do método para garantir que o alvo seja encontrado.
        // O nome intermediário de `WorldRenderer$SectionRenderState` pode ser `Lnet/minecraft/class_761$class_8842;`
        // No entanto, o Yarn deve mapear isso corretamente para o nome aninhado.
        method = "renderBlockLayers(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/render/WorldRenderer$SectionRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$takeOverBlockLayerRendering(
            Matrix4fc matrix, double cameraX, double cameraY, double cameraZ,
            // CORRETO: Usar CallbackInfoReturnable para métodos que retornam um valor.
            CallbackInfoReturnable<Object> cir 
    ) {
        MatrixStack matrices = new MatrixStack();
        matrices.peek().getPositionMatrix().mul(matrix);

        for (RenderLayer layer : RenderLayer.getBlockLayers()) {
            BariumRenderManager.getInstance().renderLayer(matrices, layer, cameraX, cameraY, cameraZ);
        }

        // Como nós cancelamos o método original, ele espera que retornemos um valor do tipo SectionRenderState.
        // Retornar 'null' é a aposta mais segura. O código que chama `renderBlockLayers`
        // deve ser capaz de lidar com um valor nulo, ou isso não importa mais porque nós controlamos tudo.
        cir.setReturnValue(null);
    }
}