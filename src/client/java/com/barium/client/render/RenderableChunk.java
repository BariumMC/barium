package com.barium.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderableChunk {

    public final BlockPos origin;
    private final Map<RenderLayer, StreamingBuffer.Region> regions = new ConcurrentHashMap<>();
    private final AtomicBoolean needsRebuild = new AtomicBoolean(true);
    private ChunkMesher.Result lastMeshResult = null;

    public RenderableChunk(int x, int y, int z) {
        this.origin = new BlockPos(x, y, z);
    }

    public void setNeedsRebuild() {
        this.needsRebuild.set(true);
    }

    public boolean needsRebuild() {
        return this.needsRebuild.getAndSet(false);
    }

    public void setMeshResult(ChunkMesher.Result result) {
        this.lastMeshResult = result;
    }

    public ChunkMesher.Result getMeshResult() {
        return this.lastMeshResult;
    }

    public void upload(RenderLayer layer, StreamingBuffer.Region region) {
        this.regions.put(layer, region);
    }

    public void draw(RenderLayer layer) {
        StreamingBuffer.Region region = this.regions.get(layer);
        if (region != null && region.getVertexCount() > 0) {
            GL11.glDrawArrays(GL11.GL_QUADS, (int) (region.offset() / BariumVertexFormat.STRIDE), region.getVertexCount());
        }
    }

    public void delete() {
        this.regions.clear();
        if (this.lastMeshResult != null) {
            this.lastMeshResult.free();
            this.lastMeshResult = null;
        }
    }
}