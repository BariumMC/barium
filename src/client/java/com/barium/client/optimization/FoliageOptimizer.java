package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.concurrent.ThreadLocalRandom;

public class FoliageOptimizer {

    public enum FoliageLod {
        FULL,               // Renderização completa (padrão)
        CROSS_WITH_CULLING, // Modelo cruzado, mas com back-face culling
        FLAT,               // Renderização como sprite plano (apenas uma face)
        CULLED              // Não renderizar nada
    }

    private static final double LOD_FLAT_DISTANCE_SQ = 32.0 * 32.0;
    private static final double LOD_CULLED_DISTANCE_SQ = 64.0 * 64.0;

    public static void init() {
        BariumMod.LOGGER.info("Initializing Aggressive FoliageOptimizer with Face Culling");
    }

    public static FoliageLod getFoliageLod(BlockState state, BlockPos pos) {
        if (!BariumConfig.C.ENABLE_DENSE_FOLIAGE_CULLING || !isDenseFoliage(state)) {
            return FoliageLod.FULL;
        }

        if (ThreadLocalRandom.current().nextInt(5) < BariumConfig.C.DENSE_FOLIAGE_CULLING_LEVEL) {
            return FoliageLod.CULLED;
        }

        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        double distanceSq = cameraPos.squaredDistanceTo(pos.toCenterPos());

        if (distanceSq > LOD_CULLED_DISTANCE_SQ) {
            return FoliageLod.CULLED;
        }
        if (distanceSq > LOD_FLAT_DISTANCE_SQ) {
            return FoliageLod.FLAT;
        }
        
        // Para folhagens próximas, usamos o modelo cruzado, mas com culling de face.
        return FoliageLod.CROSS_WITH_CULLING;
    }
    
    public static boolean isDenseFoliage(BlockState state) {
        return state.isOf(Blocks.SHORT_GRASS) ||
               state.isOf(Blocks.FERN) ||
               state.isOf(Blocks.TALL_GRASS) ||
               state.isOf(Blocks.LARGE_FERN);
    }
}