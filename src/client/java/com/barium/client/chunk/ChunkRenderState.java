package com.barium.client.chunk;

import net.minecraft.util.math.ChunkPos;

public final class ChunkRenderState {
    private final ChunkPos pos;
    private boolean visible;
    private float priorityScore;
    private int lastMeshQueueFrame = Integer.MIN_VALUE;

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

    public boolean markQueuedThisFrame(int frameId) {
        if (lastMeshQueueFrame == frameId) {
            return false;
        }
        lastMeshQueueFrame = frameId;
        return true;
    }
}
