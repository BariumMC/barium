package com.barium.client.render;

import com.barium.BariumMod;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;

    public void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        BariumMod.LOGGER.info("Barium Render Manager inicializado.");
    }

    public void renderLayer(MatrixStack matrices, RenderLayer layer, double cameraX, double cameraY, double cameraZ) {
        layer.startDrawing();
        // TODO: Sua lógica de renderização para este layer.
        layer.endDrawing();
    }

    public void onWorldChange(@Nullable ClientWorld newWorld) {}
    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {}

    public void shutdown() {
        if (this.mesherExecutor != null) this.mesherExecutor.shutdown();
        if (this.streamingBuffer != null) this.streamingBuffer.delete();
    }
}