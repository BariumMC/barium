package com.barium.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderableChunk {
    public final BlockPos origin;
    private final Box boundingBox;
    private final Map<RenderLayer, StreamingBuffer.Region> regions = new ConcurrentHashMap<>();
    private final AtomicBoolean needsRebuild = new AtomicBoolean(true);
    private ChunkMesher.Result lastMeshResult = null;

    public RenderableChunk(BlockPos origin) {
        this.origin = origin;
        this.boundingBox = new Box(
            origin.getX(), origin.getY(), origin.getZ(),
            origin.getX() + 16, origin.getY() + 16, origin.getZ() + 16
        );
    }

    public BlockPos getOrigin() { return this.origin; }
    public Box getBoundingBox() { return this.boundingBox; }
    public boolean needsRebuild() { return this.needsRebuild.getAndSet(false); }
    public void setMeshResult(ChunkMesher.Result result) { this.lastMeshResult = result; }
    public ChunkMesher.Result getMeshResult() { return this.lastMeshResult; }
    public void upload(RenderLayer layer, StreamingBuffer.Region region) { this.regions.put(layer, region); }
    
    public void delete() {
        this.regions.clear(); // As regiões no VBO serão sobrescritas, não precisam de limpeza
    }

    public void draw(RenderLayer layer) {
        StreamingBuffer.Region region = this.regions.get(layer);
        if (region != null && region.getVertexCount() > 0) {
            GL11.glDrawArrays(GL11.GL_QUADS, (int) (region.offset() / BariumVertexFormat.STRIDE), region.getVertexCount());
        }
    }
}