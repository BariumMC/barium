package com.barium.client.render.region;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.BlockPos;

// Representa uma região do mundo pronta para ser renderizada pela GPU.
public class RenderRegion {
    public static final int REGION_SIZE = 48; // 3 chunks de 16 blocos

    private final BlockPos origin;
    private VertexBuffer solidMesh; // VBO para blocos opacos
    private VertexBuffer translucentMesh; // VBO para blocos translúcidos

    private boolean needsRebuild = true;
    private boolean isEmpty = true;

    public RenderRegion(int x, int y, int z) {
        this.origin = new BlockPos(x, y, z);
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public boolean needsRebuild() {
        return this.needsRebuild;
    }

    public void markForRebuild() {
        this.needsRebuild = true;
    }

    // Recebe os dados da malha construída pelo RegionMeshBuilder e os envia para a GPU.
    public void upload(RegionBuildResult result) {
        this.needsRebuild = false;
        this.isEmpty = result.solidVertexCount == 0 && result.translucentVertexCount == 0;

        if (this.isEmpty) {
            return;
        }

        // Upload da malha sólida
        if (result.solidVertexCount > 0) {
            if (this.solidMesh == null) this.solidMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.solidMesh.bind();
            this.solidMesh.upload(result.solidBuilder.end());
        }

        // Upload da malha translúcida
        if (result.translucentVertexCount > 0) {
            if (this.translucentMesh == null) this.translucentMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.translucentMesh.bind();
            this.translucentMesh.upload(result.translucentBuilder.end());
        }
    }

    // Desenha a malha na tela. Esta é a "draw call".
    public void renderSolid() {
        if (!isEmpty && solidMesh != null) {
            this.solidMesh.bind();
            // A configuração do formato de vértice acontece aqui
            this.solidMesh.draw(RenderSystem.getMVP(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
    }

    public void renderTranslucent() {
        if (!isEmpty && translucentMesh != null) {
            this.translucentMesh.bind();
            this.translucentMesh.draw(RenderSystem.getMVP(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
    }

    // Libera a memória da GPU quando a região é descarregada.
    public void dispose() {
        if (solidMesh != null) solidMesh.close();
        if (translucentMesh != null) translucentMesh.close();
    }
}