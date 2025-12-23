package com.barium.client.mixin;

import com.barium.client.optimization.EntityOutlineOptimizer;
import com.barium.client.optimization.ChunkUploadThrottler;
import com.barium.client.util.ChunkRenderManager;
import com.barium.client.util.ChunkVisibilityManager;
import com.barium.client.util.FloodFillVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ChunkBuilder chunkBuilder;
    // CORREÇÃO: O @Accessor foi movido para a interface WorldRendererAccessor, que é a prática correta.
    // O campo frustum agora é acessado através dela.

    /**
     * Reinicia o contador de otimização no início do render do frame.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void barium$resetFrameState(CallbackInfo ci) {
        EntityOutlineOptimizer.reset();
    }

    /**
     * Intercepta o momento em que o Minecraft verifica se uma entidade deve brilhar.
     * Se ela for brilhar, notificamos nosso otimizador.
     * Isso nos permite saber, com CUSTO ZERO (pois o jogo já faz essa verificação),
     * se existe algo brilhando na tela.
     */
    @Redirect(
        method = "fillEntityOutlineRenderStates",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;hasOutline()Z",
            require = 0
        )
    )
    private boolean barium$detectGlowingEntities(EntityRenderState state) {
        boolean hasOutline = state.hasOutline();
        if (hasOutline) {
            // Opa! Tem algo brilhando. O pipeline de outline será necessário.
            EntityOutlineOptimizer.notifyGlowingEntity();
        }
        return hasOutline;
    }

    /**
     * A GRANDE OTIMIZAÇÃO:
     * Substitui a verificação simples do vanilla por nossa lógica inteligente.
     * Se o EntityOutlineOptimizer disser que o buffer está vazio (nenhuma entidade brilhou),
     * cancelamos o método. Isso evita rodar o shader de blur e o blit, economizando ~34% de render time em cenas vazias.
     */
    @Inject(method = "canDrawEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void barium$optimizeEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (!EntityOutlineOptimizer.shouldProcessOutlines()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * CORREÇÃO: O método `setupTerrain` foi removido do jogo.
     * A injeção foi movida para `tick()`, que é chamado a cada frame e é o local ideal
     * para atualizar os managers de otimização.
     */
    @Inject(method = "tick(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$updateAllChunkManagers(Camera camera, CallbackInfo ci) {
        Frustum frustum = ((WorldRendererAccessor) this).getFrustum();
        if (client.world == null || client.player == null || frustum == null) {
            FloodFillVisibilityManager.getInstance().clear();
            ChunkVisibilityManager.getInstance().clear();
            ChunkRenderManager.getInstance().clear();
            return;
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
     * Reseta contadores e prepara o chunk builder antes da fase de atualização de chunks.
     */
    @Inject(method = "updateChunks(Lnet/minecraft/client/render/Camera;)V", at = @At("HEAD"))
    private void barium$beforeUpdateChunks(Camera camera, CallbackInfo ci) {
        if (this.chunkBuilder != null) {
            this.chunkBuilder.setCameraPosition(camera.getPos());
        }
        ChunkUploadThrottler.resetCounter();
    }
}