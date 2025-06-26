package com.barium.client.render.region;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MatrixStack;
import net.minecraft.world.World;

// Gerencia todas as RenderRegions do mundo.
public class RegionRenderer {
    private final World world;
    private final Long2ObjectMap<RenderRegion> regions = new Long2ObjectOpenHashMap<>();
    private final RegionMeshBuilder meshBuilder = new RegionMeshBuilder();

    public RegionRenderer(World world) {
        this.world = world;
    }

    // Chamado a cada frame para atualizar o estado.
    public void update(Camera camera, Frustum frustum) {
        // 1. Descarregar regiões distantes
        // TODO: Implementar lógica para encontrar e descarregar regiões longe do jogador.
        
        // 2. Carregar novas regiões perto do jogador
        // TODO: Implementar lógica para criar novas RenderRegions nas áreas para onde o jogador se moveu.

        // 3. Reconstruir regiões "sujas"
        for (RenderRegion region : regions.values()) {
            if (region.needsRebuild()) {
                RegionBuildResult result = meshBuilder.build(world, region);
                region.upload(result);
            }
        }
    }

    // Chamado para renderizar uma camada (ex: sólida, translúcida).
    public void render(RenderLayer layer, MatrixStack matrices, double camX, double camY, double camZ) {
        for (RenderRegion region : regions.values()) {
            // TODO: Implementar Frustum Culling aqui, para checar se a região está visível.

            matrices.push();
            matrices.translate(region.getOrigin().getX() - camX, 
                               region.getOrigin().getY() - camY, 
                               region.getOrigin().getZ() - camZ);

            if (layer == RenderLayer.getSolid()) {
                region.renderSolid();
            } else if (layer == RenderLayer.getTranslucent()) {
                region.renderTranslucent();
            }
            // ... outras camadas

            matrices.pop();
        }
    }

    // Chamado quando um bloco muda no mundo.
    public void onBlockUpdate(BlockPos pos) {
        int regionX = Math.floorDiv(pos.getX(), RenderRegion.REGION_SIZE) * RenderRegion.REGION_SIZE;
        int regionY = Math.floorDiv(pos.getY(), RenderRegion.REGION_SIZE) * RenderRegion.REGION_SIZE;
        int regionZ = Math.floorDiv(pos.getZ(), RenderRegion.REGION_SIZE) * RenderRegion.REGION_SIZE;
        
        long key = BlockPos.asLong(regionX, regionY, regionZ);
        RenderRegion region = regions.get(key);
        if (region != null) {
            region.markForRebuild();
        }
        
        // TODO: Também marcar regiões vizinhas como sujas se a mudança for na borda.
    }

    public void dispose() {
        for (RenderRegion region : regions.values()) {
            region.dispose();
        }
        regions.clear();
    }
}