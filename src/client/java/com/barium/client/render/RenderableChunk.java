package com.barium.client.render;

import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderableChunk {
    public final BlockPos origin;
    private final Box boundingBox;
    private final Map<RenderLayer, BuiltBuffer> geometry = new ConcurrentHashMap<>();
    private final AtomicBoolean needsRebuild = new AtomicBoolean(true);

    public RenderableChunk(BlockPos origin) {
        this.origin = origin;
        this.boundingBox = new Box(
            origin.getX(), origin.getY(), origin.getZ(),
            origin.getX() + 16, origin.getY() + 16, origin.getZ() + 16
        );
    }

    public Box getBoundingBox() { return this.boundingBox; }
    public boolean needsRebuild() { return this.needsRebuild.getAndSet(false); }
    
    public void setGeometry(Map<RenderLayer, BuiltBuffer> newGeometry) {
        this.delete(); // Limpa a geometria antiga
        this.geometry.putAll(newGeometry);
    }
    
    public void delete() {
        this.geometry.values().forEach(BuiltBuffer::close);
        this.geometry.clear();
    }

    public void draw(RenderLayer layer) {
        BuiltBuffer buffer = this.geometry.get(layer);
        if (buffer != null) {
            layer.draw(buffer);
        }
    }
}