package com.barium.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BariumRenderManager {
    private static final BariumRenderManager INSTANCE = new BariumRenderManager();
    public static BariumRenderManager getInstance() { return INSTANCE; }

    private ExecutorService mesherExecutor;
    private StreamingBuffer streamingBuffer;
    
    // TODO: Adicionar um mapa para gerenciar os `RenderableChunk`s
    // private Map<ChunkPos, RenderableChunk> renderChunks;
    
    public void init() {
        // Cria um pool de threads para o meshing, usando todos os núcleos disponíveis.
        this.mesherExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            r -> new Thread(r, "Barium Mesher Thread"));
            
        // Aloca 256MB para nosso buffer de vértices na GPU.
        this.streamingBuffer = new StreamingBuffer(256 * 1024 * 1024); 
    }

    public void renderLayer(MatrixStack matrices, RenderLayer layer) {
        // TODO: Esta é a nova função de renderização.
        
        // 1. Faça o bind do nosso `streamingBuffer`.
        this.streamingBuffer.bind();
        
        // 2. Configure os atributos de vértice para o nosso `BariumVertexFormat`.
        //    (glVertexAttribPointer)

        // 3. Itere sobre todos os `RenderableChunk`s visíveis.
        
        // 4. Para cada chunk, chame `glDrawArrays` usando as informações da sua `Region`.
        //    Ou, para performance máxima, construa um comando `MultiDrawIndirect`.
        
        // 5. Desfaça o bind e desative os atributos de vértice.
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