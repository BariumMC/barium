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

    // CORREÇÃO FINAL: RenderLayer.getTranslucent() foi movido. Agora o acesso é direto.
    // Vamos definir os layers que nosso renderizador customizado vai lidar.
    private static final List<RenderLayer> CHUNK_LAYERS = List.of(
        RenderLayer.SOLID, 
        RenderLayer.CUTOUT_MIPPED, 
        RenderLayer.CUTOUT, 
        RenderLayer.TRANSLUCENT
        // RenderLayer.TRIPWIRE pode ser necessário também
    );

    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;

    public void init() {
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024);
        BariumMod.LOGGER.info("Barium Render Manager inicializado.");
    }
    
    // Este método é chamado pelo nosso @Redirect para CADA layer.
    public void renderLayer(MatrixStack matrices, RenderLayer layer, double cameraX, double cameraY, double cameraZ) {
        // Começa a desenhar no estado correto para o layer.
        layer.startDrawing();

        // TODO: Sua lógica de renderização para este layer.
        // - Bind do StreamingBuffer
        // - Configuração de atributos de vértice (glVertexAttribPointer)
        // - Chamadas de desenho (glMultiDrawIndirect ou glDrawArrays)

        // Termina o desenho, resetando o estado.
        layer.endDrawing();
    }
    
    // Métodos de ciclo de vida e agendamento
    public void onWorldChange(@Nullable ClientWorld newWorld) {}
    public void scheduleRebuild(int x, int y, int z, boolean isPriority) {}

    public void shutdown() {
        if (this.mesherExecutor != null) this.mesherExecutor.shutdown();
        if (this.streamingBuffer != null) this.streamingBuffer.delete();
    }
}