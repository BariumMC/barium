package com.barium.client.chunk;

import net.minecraft.util.math.ChunkPos;

public final class ChunkRenderState {
    private final ChunkPos pos;
    private boolean visible;
    private float priorityScore;

    public ChunkRenderState(ChunkPos pos) {
        this.pos = pos;
    }

    public ChunkPos pos() {
        return pos;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float priorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(float priorityScore) {
        this.priorityScore = priorityScore;
    }
}
