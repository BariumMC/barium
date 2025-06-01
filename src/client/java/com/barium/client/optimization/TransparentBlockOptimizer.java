package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza a renderização de blocos transparentes, aplicando culling e LOD para folhas.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
public class TransparentBlockOptimizer {

    public static void init() {
        BariumMod.LOGGER.info("Inicializando TransparentBlockOptimizer");
    }

    /**
     * Determina se um bloco transparente genérico (incluindo folhas) deve ser totalmente ocultado.
     * Isso é para o recurso `ENABLE_TRANSPARENT_BLOCK_CULLING`.
     *
     * @param state O BlockState sendo renderizado.
     * @param pos A posição do bloco.
     * @param world O mundo do cliente.
     * @return true se o bloco deve ser culled (não renderizado), false se deve ser renderizado.
     */
    public static boolean shouldCullTransparentBlock(BlockState state, BlockPos pos, ClientWorld world) {
        if (!BariumConfig.ENABLE_TRANSPARENT_BLOCK_OPTIMIZATION || !BariumConfig.ENABLE_TRANSPARENT_BLOCK_CULLING) {
            return false; // Não culla se a otimização ou o culling estiverem desativados
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        // Verifica se o bloco é considerado translúcido pela lógica interna do Minecraft.
        // Isso é uma boa heurística para blocos transparentes.
        boolean isMinecraftTranslucent = state.isTranslucent(world, pos);

        if (!isMinecraftTranslucent) {
            return false; // Culla apenas blocos que o Minecraft considera transparentes
        }

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        double distanceSq = cameraPos.squaredDistanceTo(blockCenter);

        return distanceSq > BariumConfig.TRANSPARENT_BLOCK_CULLING_DISTANCE * BariumConfig.TRANSPARENT_BLOCK_CULLING_DISTANCE;
    }

    /**
     * Determina se um LeavesBlock (bloco de folhas) deve ser renderizado como opaco devido ao Nível de Detalhe (LOD).
     * Isso é para o recurso `ENABLE_LEAVES_LOD`.
     *
     * @param state O BlockState das folhas.
     * @param pos A posição das folhas.
     * @param world O mundo do cliente.
     * @return true se as folhas devem ser renderizadas opacas, false se devem renderizar como translúcidas/recortadas.
     */
    public static boolean shouldRenderLeavesAsOpaqueLOD(BlockState state, BlockPos pos, ClientWorld world) {
        if (!BariumConfig.ENABLE_TRANSPARENT_BLOCK_OPTIMIZATION || !BariumConfig.ENABLE_LEAVES_LOD || !(state.getBlock() instanceof LeavesBlock)) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        double distanceSq = cameraPos.squaredDistanceTo(blockCenter);

        return distanceSq > BariumConfig.LEAVES_LOD_DISTANCE * BariumConfig.LEAVES_LOD_DISTANCE;
    }
}