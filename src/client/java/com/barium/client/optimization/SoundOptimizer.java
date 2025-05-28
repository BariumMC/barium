package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Otimiza o sistema de som, filtrando sons não audíveis com base na distância e obstruções.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Adicionado método init() e shouldPlaySound() para compatibilidade com o mixin.
 */
public class SoundOptimizer {

    // Distância máxima para sons serem audíveis (quadrada)
    private static final double MAX_SOUND_DISTANCE_SQ = 64 * 64; // 64 blocos
    // Número máximo de obstruções para um som ser audível
    private static final int MAX_OBSTRUCTIONS = 5;

    /**
     * Inicializa o otimizador de som.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando SoundOptimizer");
    }

    /**
     * Verifica se uma instância de som deve ser reproduzida com base na distância e obstrução.
     * Método usado diretamente pelo mixin SoundSystemMixin.
     *
     * @param sound A instância de som.
     * @param player O jogador.
     * @return true se o som deve ser reproduzido, false se deve ser pulado.
     */
    public static boolean shouldPlaySound(SoundInstance sound, PlayerEntity player) {
        if (!BariumConfig.ENABLE_SOUND_OPTIMIZATION) {
            return true; // Reproduz normalmente se a otimização está desligada
        }

        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world == null) {
            return true; // Não pode otimizar sem mundo
        }

        // Obtém a posição do som
        Vec3d soundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
        Vec3d playerPos = player.getPos();

        // 1. Verificação de Distância
        double distanceSq = playerPos.squaredDistanceTo(soundPos);
        if (distanceSq > MAX_SOUND_DISTANCE_SQ * sound.getVolume() * sound.getVolume()) { // Considera o volume
            // BariumMod.LOGGER.debug("Skipping sound {} due to distance", sound.getId());
            return false; // Som muito distante, não reproduzir
        }

        // 2. Verificação de Obstrução (Raycasting Simplificado)
        if (BariumConfig.ENABLE_SOUND_OBSTRUCTION_CHECK) {
            int obstructions = countObstructions(world, player.getCameraPosVec(1.0f), soundPos);
            if (obstructions > MAX_OBSTRUCTIONS) {
                // BariumMod.LOGGER.debug("Skipping sound {} due to obstruction ({})", sound.getId(), obstructions);
                return false; // Som muito obstruído, não reproduzir
            }
        }

        return true; // Som deve ser reproduzido
    }

    /**
     * Verifica se uma instância de som deve ser pulada com base na distância e obstrução.
     * Método alternativo para uso interno.
     *
     * @param soundSystem O sistema de som.
     * @param instance A instância de som.
     * @return true se o som deve ser pulado.
     */
    public static boolean shouldSkipSound(SoundSystem soundSystem, SoundInstance instance) {
        if (!BariumConfig.ENABLE_SOUND_OPTIMIZATION) {
            return false; // Não pula se a otimização está desligada
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        if (player == null) {
            return false; // Não pode otimizar sem jogador
        }

        // Usa a implementação de shouldPlaySound, mas inverte o resultado
        return !shouldPlaySound(instance, player);
    }

    /**
     * Conta o número aproximado de blocos sólidos entre dois pontos (Raycasting simplificado).
     *
     * @param world O mundo.
     * @param start Ponto inicial (olhos do jogador).
     * @param end Ponto final (posição do som).
     * @return O número de blocos sólidos encontrados.
     */
    private static int countObstructions(World world, Vec3d start, Vec3d end) {
        int obstructions = 0;
        Vec3d direction = end.subtract(start).normalize();
        double maxDistance = start.distanceTo(end);

        // Itera ao longo do raio
        for (double d = 1.0; d < maxDistance; d += 1.0) { // Passo de 1 bloco
            Vec3d currentPosVec = start.add(direction.multiply(d));
            BlockPos currentBlockPos = BlockPos.ofFloored(currentPosVec.x, currentPosVec.y, currentPosVec.z);

            // Verifica se o bloco é sólido (obstrui o som)
            if (world.getBlockState(currentBlockPos).isSolidBlock(world, currentBlockPos)) {
                 obstructions++;
            }
            
            // Limite para evitar processamento excessivo
            if (obstructions > MAX_OBSTRUCTIONS) {
                break;
            }
        }

        return obstructions;
    }
}
