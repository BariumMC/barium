package com.barium.client.render.region;

import net.minecraft.client.render.BufferBuilder;

// Contém o resultado do processo de construção da malha.
public class RegionBuildResult {
    public final BufferBuilder solidBuilder;
    public final int solidVertexCount;
    public final BufferBuilder translucentBuilder;
    public final int translucentVertexCount;

    public RegionBuildResult(BufferBuilder solid, int solidCount, BufferBuilder translucent, int translucentCount) {
        this.solidBuilder = solid;
        this.solidVertexCount = solidCount;
        this.translucentBuilder = translucent;
        this.translucentVertexCount = translucentCount;
    }
}