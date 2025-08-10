package com.barium.client.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderableChunk {
    public final BlockPos origin;
    private final Box boundingBox;
    // O nome foi mudado para refletir que guardamos o ByteBuffer, não uma Region.
    private final Map<RenderLayer, ByteBuffer> geometry = new ConcurrentHashMap<>();
    private final AtomicBoolean needsRebuild = new AtomicBoolean(true);
    private ChunkMesher.Result lastMeshResult = null;

    public RenderableChunk(BlockPos origin) {
        this.origin = origin;
        this.boundingBox = new Box(
            origin.getX(), origin.getY(), origin.getZ(),
            origin.getX() + 16, origin.getY() + 16, origin.getZ() + 16
        );
    }

    public Box getBoundingBox() { return this.boundingBox; }
    public boolean needsRebuild() { return this.needsRebuild.getAndSet(false); }
    public void setMeshResult(ChunkMesher.Result result) { this.lastMeshResult = result; }
    public ChunkMesher.Result getMeshResult() { return this.lastMeshResult; }
    
    // O upload agora recebe um ByteBuffer diretamente.
    public void upload(RenderLayer layer, ByteBuffer buffer) {
        this.geometry.put(layer, buffer);
    }
    
    public void delete() {
        this.geometry.clear();
        if (this.lastMeshResult != null) {
            this.lastMeshResult.free();
            this.lastMeshResult = null;
        }
    }

    public void draw(RenderLayer layer) {
        ByteBuffer buffer = this.geometry.get(layer);
        if (buffer != null && buffer.remaining() > 0) {
            // A FORMA CORRETA DE DESENHAR
            // 1. Obter o formato de vértice do layer
            VertexFormat vertexFormat = layer.getVertexFormat();
            
            // 2. Construir um BufferBuilder COM o formato correto.
            BufferBuilder builder = new BufferBuilder(buffer.capacity());
            
            // A CORREÇÃO: Inicializamos o builder antes de passar os dados.
            builder.begin(layer.getDrawMode(), vertexFormat);
            
            // 3. Passa nossos dados brutos para o builder
            builder.read(buffer.asReadOnlyBuffer());
            
            // 4. Finaliza o buffer para obter um BuiltBuffer válido
            BuiltBuffer builtBuffer = builder.end();

            // 5. Chamar o método draw do layer com o buffer construído.
            if (builtBuffer != null) {
                layer.draw(builtBuffer);
            }
        }
    }
}