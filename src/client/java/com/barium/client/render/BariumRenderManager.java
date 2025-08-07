package com.barium.client.render;

import com.barium.BariumMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List; // Importar a lista

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    // CORREÇÃO: A lista de layers de bloco não é mais pública. Nós a definimos aqui.
    private static final List<RenderLayer> CHUNK_LAYERS = List.of(
        RenderLayer.getSolid(), 
        RenderLayer.getCutoutMipped(), 
        RenderLayer.getCutout(), 
        RenderLayer.getTranslucent()
    );

    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;

    public void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        BariumMod.LOGGER.info("Barium Render Manager inicializado.");
    }

    /**
     * Esta é a nossa NOVA função de renderização principal. Ela substitui o `WorldRenderer.render`.
     */
    public void renderWorld(MatrixStack matrices, Camera camera) {
        double cameraX = camera.getPos().getX();
        double cameraY = camera.getPos().getY();
        double cameraZ = camera.getPos().getZ();

        // 1. Configurar estado do OpenGL (profundidade, culling, etc.)
        // Ex: GlStateManager._enableDepthTest();

        // 2. Loop através dos layers de renderização
        for (RenderLayer layer : CHUNK_LAYERS) {
            // 3. Chamar nosso método de renderização específico do layer
            this.renderLayer(matrices, layer, cameraX, cameraY, cameraZ);
        }

        // 4. Limpar o estado do OpenGL
    }
    
    // Método para renderizar um único layer (não precisa de muitas mudanças)
    private void renderLayer(MatrixStack matrices, RenderLayer layer, double cameraX, double cameraY, double cameraZ) {
        layer.startDrawing();

        // TODO: Sua lógica de renderização para este layer
        // - Bind do StreamingBuffer
        // - Configuração de atributos de vértice
        // - Chamadas de desenho (glMultiDrawIndirect)

        layer.endDrawing();
    }
    
    // Métodos de ciclo de vida e agendamento
    public void onWorldChange(@Nullable ClientWorld newWorld) {
        // Lógica de limpeza
    }
    
    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {
        // Lógica de agendamento
    }

    public void shutdown() {
        if (this.mesherExecutor != null) {
            this.mesherExecutor.shutdown();
        }
        if (this.streamingBuffer != null) {
            this.streamingBuffer.delete();
        }
    }
}