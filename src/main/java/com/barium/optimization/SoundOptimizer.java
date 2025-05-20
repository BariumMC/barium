package com.barium.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Otimizador de som e áudio.
 * 
 * Implementa:
 * - Filtragem de sons não audíveis com base na distância real e obstruções
 */
public class SoundOptimizer {
    // Mapa para controlar os sons já verificados
    private static final Map<SoundInstance, Boolean> SOUND_AUDIBILITY_CACHE = new HashMap<>();
    
    public static void init() {
        BariumMod.LOGGER.info("Inicializando otimizações de som e áudio");
    }
    
    /**
     * Verifica se um som deve ser processado e reproduzido
     * 
     * @param sound A instância do som
     * @param player O jogador
     * @return true se o som deve ser reproduzido, false caso contrário
     */
    public static boolean shouldPlaySound(SoundInstance sound, PlayerEntity player) {
        if (!BariumConfig.ENABLE_SOUND_CULLING) {
            return true;
        }
        
        // Verifica se já temos um resultado em cache para este som
        Boolean cached = SOUND_AUDIBILITY_CACHE.get(sound);
        if (cached != null) {
            return cached;
        }
        
        // Obtém as posições do som e do jogador
        Vec3d soundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
        Vec3d playerPos = player.getPos();
        
        // Calcula a distância
        double distance = soundPos.distanceTo(playerPos);
        
        // Culling baseado na distância
        if (distance > BariumConfig.SOUND_CULLING_DISTANCE) {
            SOUND_AUDIBILITY_CACHE.put(sound, false);
            return false;
        }
        
        // Verifica obstruções entre o jogador e a fonte do som
        if (distance > 5.0 && hasObstructions(player, soundPos)) {
            // Reduz o volume com base nas obstruções
            float volumeReduction = calculateVolumeReduction(player, soundPos);
            
            // Se o volume for muito baixo após a redução, não reproduz o som
            if (sound.getVolume() * volumeReduction < 0.05f) {
                SOUND_AUDIBILITY_CACHE.put(sound, false);
                return false;
            }
        }
        
        // Som deve ser reproduzido
        SOUND_AUDIBILITY_CACHE.put(sound, true);
        return true;
    }
    
    /**
     * Verifica se há obstruções sólidas entre o jogador e a fonte do som
     * 
     * @param player O jogador
     * @param soundPos A posição da fonte do som
     * @return true se há obstruções, false caso contrário
     */
    private static boolean hasObstructions(PlayerEntity player, Vec3d soundPos) {
        // Simplificação: verifica apenas alguns pontos ao longo da linha
        Vec3d playerPos = player.getEyePos();
        Vec3d direction = soundPos.subtract(playerPos).normalize();
        double distance = playerPos.distanceTo(soundPos);
        
        // Verifica pontos ao longo da linha
        int steps = Math.min(10, (int)(distance / 2.0));
        for (int i = 1; i < steps; i++) {
            double t = i / (double)steps;
            Vec3d point = playerPos.add(direction.multiply(distance * t));
            BlockPos blockPos = new BlockPos((int)point.x, (int)point.y, (int)point.z);
            
            // Verifica se o bloco é sólido
            if (player.getWorld().getBlockState(blockPos).isSolid()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calcula a redução de volume com base nas obstruções
     * 
     * @param player O jogador
     * @param soundPos A posição da fonte do som
     * @return O fator de redução de volume (0.0 - 1.0)
     */
    private static float calculateVolumeReduction(PlayerEntity player, Vec3d soundPos) {
        // Simplificação: conta o número de blocos sólidos no caminho
        Vec3d playerPos = player.getEyePos();
        Vec3d direction = soundPos.subtract(playerPos).normalize();
        double distance = playerPos.distanceTo(soundPos);
        
        int solidBlocks = 0;
        int steps = Math.min(20, (int)(distance / 1.0));
        
        for (int i = 1; i < steps; i++) {
            double t = i / (double)steps;
            Vec3d point = playerPos.add(direction.multiply(distance * t));
            BlockPos blockPos = new BlockPos((int)point.x, (int)point.y, (int)point.z);
            
            // Conta blocos sólidos
            if (player.getWorld().getBlockState(blockPos).isSolid()) {
                solidBlocks++;
            }
        }
        
        // Cada bloco sólido reduz o volume em 20%
        float reduction = 1.0f - (solidBlocks * 0.2f);
        return Math.max(0.0f, reduction);
    }
    
    /**
     * Limpa o cache de audibilidade
     * Deve ser chamado periodicamente para evitar vazamentos de memória
     */
    public static void clearAudibilityCache() {
        SOUND_AUDIBILITY_CACHE.clear();
    }
    
    /**
     * Remove um som do cache
     * 
     * @param sound A instância do som a remover
     */
    public static void removeSound(SoundInstance sound) {
        SOUND_AUDIBILITY_CACHE.remove(sound);
    }
}
