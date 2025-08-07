package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin de Tomada de Controle Total (VERSÃO FINAL COM @OVERWRITE)
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // --- Referências para os métodos e campos originais que queremos usar ---
    @Shadow private boolean shouldCaptureFrustum;
    @Shadow private Frustum frustum;
    @Shadow protected abstract void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl);
    @Shadow protected abstract void renderClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ);
    @Shadow protected abstract void renderWeather(MatrixStack matrices, LightmapTextureManager lightmapTextureManager, float tickDelta, double cameraX, double cameraY, double cameraZ);
    @Shadow protected abstract void renderWorldBorder(Camera camera);
    @Shadow protected abstract void renderEntities(MatrixStack matrices, Camera camera, Frustum frustum, RenderTickCounter tickCounter);
    // Adicione outros @Shadows se precisar (ex: renderBlockDamage, renderParticles)

    /**
     * @author Barium
     * @reason Substituição completa do método de renderização principal para implementar
     *          um pipeline de renderização customizado e otimizado.
     * 
     * @Overwrite apaga completamente o método `render` original e o substitui por este.
     *            Isso nos dá controle total sobre o que é desenhado e em que ordem.
     */
    @Overwrite
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        
        // --- 1. Lógica de Setup Vanilla (que podemos manter) ---
        if (this.shouldCaptureFrustum) {
            // Lógica para capturar o frustum, se necessário
        }

        // --- 2. Renderização do Fundo (Céu, Nuvens, etc.) ---
        // Nós chamamos os métodos originais para não ter que reimplementá-los.
        this.renderSky(matrices, projectionMatrix, tickDelta, camera, false);
        this.renderClouds(matrices, projectionMatrix, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);
        this.renderWeather(matrices, lightmapTextureManager, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);

        // --- 3. NOSSA RENDERIZAÇÃO DE CHUNKS ---
        // Em vez da lógica de `renderBlockLayers` do vanilla, chamamos nosso manager.
        BariumRenderManager.getInstance().renderWorld(matrices, camera);
        
        // --- 4. Renderização do Primeiro Plano (Entidades, Bordas, etc.) ---
        this.renderWorldBorder(camera);
        // this.renderEntities(matrices, camera, this.frustum, tickCounter); // tickCounter não está disponível, pode ser removido por enquanto
        
        // --- 5. Outros efeitos (se necessário) ---
        // renderBlockDamage, renderParticles, etc.
    }
}