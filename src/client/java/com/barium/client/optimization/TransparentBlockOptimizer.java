package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

/**
 * Otimiza a renderização de blocos transparentes como folhas, vidro e gelo.
 * Aplica LOD para folhas e culling para blocos transparentes distantes.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
public class TransparentBlockOptimizer {

    /**
     * Inicializa o otimizador de blocos transparentes.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando TransparentBlockOptimizer");
    }

    /**
     * Verifica se as folhas em uma dada posição devem ser simplificadas (tratadas como opacas) para LOD.
     * Esta função é chamada por um mixin e assume que está em um contexto client-side.
     *
     * @param world A visualização do bloco (pode ser ClientWorld ou ChunkRendererRegion).
     * @param pos A posição do bloco de folhas.
     * @return true se as folhas devem ser simplificadas (tratadas como opacas), false caso contrário.
     */
    public static boolean shouldSimplifyLeaves(BlockView world, BlockPos pos) {
        // Verifica se a otimização geral e a otimização de folhas estão ativadas
        if (!BariumConfig.ENABLE_TRANSPARENT_BLOCK_OPTIMIZATION || !BariumConfig.ENABLE_LEAVES_LOD) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        // Não aplica otimização se o cliente ou jogador não estiverem disponíveis
        if (client == null || client.player == null) {
            return false;
        }

        Vec3d playerPos = client.player.getPos();
        Vec3d blockCenter = Vec3d.ofCenter(pos); // Centro do bloco
        double distanceSq = playerPos.squaredDistanceTo(blockCenter); // Distância quadrada

        // Se a distância quadrada for maior que a distância de LOD configurada ao quadrado, simplifica as folhas
        return distanceSq > BariumConfig.LEAVES_LOD_DISTANCE * BariumConfig.LEAVES_LOD_DISTANCE;
    }

    /**
     * Verifica se um bloco transparente deve ser completamente ocultado (culled) com base na distância.
     * Isso pode ser usado para vidro, gelo, etc., para evitar sua renderização além de uma certa distância.
     *
     * @param world A visualização do bloco.
     * @param pos A posição do bloco transparente.
     * @return true se o bloco transparente deve ser completamente ocultado, false caso contrário.
     */
    public static boolean shouldCullTransparentBlock(BlockView world, BlockPos pos) {
        // Verifica se a otimização geral e o culling de blocos transparentes estão ativados
        if (!BariumConfig.ENABLE_TRANSPARENT_BLOCK_OPTIMIZATION || !BariumConfig.ENABLE_TRANSPARENT_BLOCK_CULLING) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return false;
        }

        Vec3d playerPos = client.player.getPos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        double distanceSq = playerPos.squaredDistanceTo(blockCenter);

        // Se a distância quadrada for maior que a distância de culling configurada ao quadrado, oculta o bloco
        return distanceSq > BariumConfig.TRANSPARENT_BLOCK_CULLING_DISTANCE * BariumConfig.TRANSPARENT_BLOCK_CULLING_DISTANCE;
    }
}