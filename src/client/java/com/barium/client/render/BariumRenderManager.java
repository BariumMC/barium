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
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            r -> new Thread(r, "Barium Mesher Thread"));
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        // CORRIGIDO: O log agora acessa o 'capacity' diretamente do buffer.
        long capacityInMB = this.streamingBuffer.capacity / (1024 * 1024);
        BariumMod.LOGGER.info("Barium Render Manager inicializado com um buffer de {} MB e {} threads.",
            capacityInMB, Runtime.getRuntime().availableProcessors());
    }

    /**
     * **CORRIGIDO:** Assinatura ajustada de volta para doubles, pois era a correta para o método renderLayer.
     */
    public void renderLayer(MatrixStack matrices, RenderLayer layer, double cameraX, double cameraY, double cameraZ) {
        // TODO: Sua lógica de renderização principal vai aqui.
        // O código anterior estava correto em passar os doubles.
    }

    public void onWorldChange(@Nullable ClientWorld newWorld) {
        BariumMod.LOGGER.info("Mundo alterado. Limpando dados do renderizador Barium.");
        // TODO: Limpar seu mapa de RenderableChunks, etc.
    }

    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {
        // TODO: Adicionar à fila de rebuild.
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