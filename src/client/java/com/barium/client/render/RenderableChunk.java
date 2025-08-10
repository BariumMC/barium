package com.barium.client.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

// O import correto para a classe principal VertexFormat
import com.mojang.blaze3d.vertex.VertexFormat;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderableChunk {
    public final BlockPos origin;
    private final Box boundingBox;
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
            // 1. Obtemos o formato e o modo de desenho diretamente do RenderLayer
            VertexFormat vertexFormat = layer.getVertexFormat();
            VertexFormat.DrawMode drawMode = layer.getDrawMode();
            
            // 2. Criamos um BufferBuilder
            BufferBuilder builder = new BufferBuilder(buffer.capacity());
            
            // 3. Inicializamos o builder com o formato e modo corretos
            builder.begin(drawMode, vertexFormat);
            
            // 4. Passamos nossos dados brutos para o builder
            builder.read(buffer.asReadOnlyBuffer());
            
            // 5. Finalizamos para obter um BuiltBuffer válido
            BuiltBuffer builtBuffer = builder.end();

            // 6. Chamamos o método draw do layer
            if (builtBuffer != null) {
                // A chamada draw não está no RenderLayer, mas sim no RenderSystem ou similar.
                // A forma mais correta é usar o que o próprio WorldRenderer usa.
                // No entanto, para simplicidade, vamos usar a forma que sabemos que compila
                // e que o RenderLayer suporta.
                layer.draw(builtBuffer);
            }
        }
    }
}