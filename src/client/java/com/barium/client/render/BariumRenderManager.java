package com.barium.client.render;

import com.barium.BariumMod;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    private static final List<RenderLayer> CHUNK_LAYERS = List.of(
        RenderLayer.getSolid(),
        RenderLayer.getCutoutMipped(),
        RenderLayer.getCutout(),
        RenderLayer.getTranslucent()
        // NOTA: Em versões futuras, RenderLayer.getTripwire() pode ser necessário aqui.
    );

    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;

    public void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        BariumMod.LOGGER.info("Barium Render Manager inicializado.");
    }

    /**
     * Função de renderização principal, agora simplificada.
     */
    public void renderWorld(MatrixStack matrices, float tickDelta) {
        // Obtenha os dados da câmera quando precisar deles, em vez de passá-los por todo lado.
        // MinecraftClient client = MinecraftClient.getInstance();
        // Camera camera = client.gameRenderer.getCamera();
        // double cameraX = camera.getPos().getX();
        // double cameraY = camera.getPos().getY();
        // double cameraZ = camera.getPos().getZ();

        // A lógica principal é iterar e desenhar cada layer.
        for (RenderLayer layer : CHUNK_LAYERS) {
            this.renderLayer(matrices, layer);
        }
    }
    
    private void renderLayer(MatrixStack matrices, RenderLayer layer) {
        layer.startDrawing();
        // TODO: Lógica de desenho para o layer.
        layer.endDrawing();
    }
    
    public void onWorldChange(@Nullable ClientWorld newWorld) {}
    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {}

    public void shutdown() {
        if (this.mesherExecutor != null) this.mesherExecutor.shutdown();
        if (this.streamingBuffer != null) this.streamingBuffer.delete();
    }
}