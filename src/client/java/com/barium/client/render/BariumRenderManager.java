package com.barium.client.render;

import com.barium.BariumMod;
import net.minecraft.client.render.Camera;
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
        BariumMod.LOGGER.info("Barium Render Manager inicializado com um buffer de {} MB e {} threads.",
            streamingBuffer.getCapacity() / (1024*1024), Runtime.getRuntime().availableProcessors());
    }

    /**
     * **CORRIGIDO:** Agora aceita o objeto Camera.
     * A função de renderização principal chamada pelo nosso Mixin.
     */
    public void renderLayer(MatrixStack matrices, RenderLayer layer, Camera camera) {
        // As coordenadas agora vêm do objeto Camera
        double cameraX = camera.getPos().getX();
        double cameraY = camera.getPos().getY();
        double cameraZ = camera.getPos().getZ();

        // TODO: Sua lógica de renderização principal vai aqui.
        // 1. Bind do streaming buffer
        // 2. Setup dos atributos de vértice (glVertexAttribPointer)
        // 3. Desenhar os chunks visíveis para este layer (glMultiDrawIndirect ou glDrawArrays)
    }

    /**
     * **ADICIONADO:** Chamado quando o mundo muda.
     */
    public void onWorldChange(@Nullable ClientWorld newWorld) {
        BariumMod.LOGGER.info("Mundo alterado. Limpando dados do renderizador Barium.");
        // TODO: Limpar seu mapa de RenderableChunks, parar todas as tarefas de meshing, etc.
    }
    
    /**
     * **ADICIONADO:** Chamado para agendar uma reconstrução de chunk.
     */
    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {
        // TODO: Adicionar a posição a uma fila para ser processada pelas threads do mesherExecutor.
        // Ex: rebuildQueue.add(new ChunkPos(x,y,z));
    }

    public void shutdown() {
        if (this.mesherExecutor != null) {
            this.mesherExecutor.shutdown();
        }
        if (this.streamingBuffer != null) {
            this.streamingBuffer.delete();
        }
    }

    // Apenas para o método de init poder logar a capacidade.
    public long getCapacity() {
        return streamingBuffer != null ? streamingBuffer.getCapacity() : 0;
    }
}