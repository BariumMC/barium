package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Otimiza animações de blocos (pistões, líquidos, etc.), pausando ou reduzindo a taxa fora de vista.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Adicionado método shouldAnimateBlockEntity() para compatibilidade com o mixin.
 */
public class AnimationCullingOptimizer {

    // Distância quadrada máxima para animações com taxa completa
    private static final double MAX_ANIMATION_DISTANCE_SQ = 48 * 48; // 48 blocos
    // Frequência de atualização para animações distantes (1 atualização a cada N ticks)
    private static final int FAR_ANIMATION_UPDATE_INTERVAL = 4;

    // Cache para o estado da animação (ativo/pausado)
    // Usamos WeakHashMap para BlockEntities que podem ser descarregados
    private static final Map<BlockEntity, AnimationState> ANIMATION_STATE_CACHE = new WeakHashMap<>();

    /**
     * Verifica se a animação de um BlockEntity deve ser atualizada neste tick.
     *
     * @param blockEntity O BlockEntity com animação.
     * @param world O mundo.
     * @return true se a atualização da animação deve ser pulada.
     */
    public static boolean shouldSkipAnimationTick(BlockEntity blockEntity, World world) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.ENABLE_ANIMATION_CULLING || world == null || !world.isClient) {
            return false; // Não otimiza no servidor ou se desligado
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        BlockPos pos = blockEntity.getPos();
        Vec3d cameraPos = camera.getPos();
        double distanceSq = cameraPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // 1. Culling por Distância
        if (distanceSq > MAX_ANIMATION_DISTANCE_SQ) {
            // Animação distante, reduz a frequência
            if ((world.getTime() + pos.hashCode()) % FAR_ANIMATION_UPDATE_INTERVAL != 0) {
                updateAnimationState(blockEntity, false); // Marca como inativo no cache
                return true; // Pula o tick
            }
        }

        // 2. Culling por Visibilidade (Frustum Simplificado - TODO: Implementar com Frustum real)
        // Box boundingBox = new Box(pos);
        // Frustum frustum = ... // Obter frustum
        // if (frustum != null && !frustum.isVisible(boundingBox)) {
        //     updateAnimationState(blockEntity, false);
        //     return true;
        // }
        
        // Se chegou aqui, a animação deve ser atualizada
        updateAnimationState(blockEntity, true); // Marca como ativo
        return false;
    }

    /**
     * Verifica se um BlockEntity deve ser animado com base na distância e visibilidade.
     * Usado pelo mixin para decidir se renderiza a animação.
     *
     * @param blockEntity O BlockEntity.
     * @param tickDelta O delta de tick parcial.
     * @return true se a animação deve ser renderizada.
     */
    public static boolean shouldAnimateBlockEntity(BlockEntity blockEntity, float tickDelta) {
        // A lógica aqui pode ser similar a shouldSkipAnimationTick, mas focada na renderização.
        // Por simplicidade, podemos apenas retornar o estado cacheado.
        return isAnimationActive(blockEntity);
    }

    /**
     * Atualiza o estado da animação no cache.
     *
     * @param blockEntity O BlockEntity.
     * @param isActive true se a animação está ativa, false se pausada/reduzida.
     */
    private static void updateAnimationState(BlockEntity blockEntity, boolean isActive) {
        AnimationState state = ANIMATION_STATE_CACHE.computeIfAbsent(blockEntity, k -> new AnimationState());
        state.isActive = isActive;
        state.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Verifica se a animação de um BlockEntity está atualmente ativa (segundo o cache).
     *
     * @param blockEntity O BlockEntity.
     * @return true se a animação deve estar ativa.
     */
    public static boolean isAnimationActive(BlockEntity blockEntity) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.ENABLE_ANIMATION_CULLING) {
            return true; // Assume ativo se otimização desligada
        }
        AnimationState state = ANIMATION_STATE_CACHE.get(blockEntity);
        // Considera ativo se não estiver no cache ou se o cache indicar ativo
        return state == null || state.isActive;
    }
    
    /**
     * Limpa o estado de um BlockEntity específico.
     */
    public static void clearBlockEntityState(BlockEntity blockEntity) {
        ANIMATION_STATE_CACHE.remove(blockEntity);
    }

    /**
     * Limpa todo o cache.
     */
    public static void clearAllCaches() {
        ANIMATION_STATE_CACHE.clear();
    }

    // --- Classe interna para Estado da Animação ---
    private static class AnimationState {
        boolean isActive = true;
        long lastUpdateTime = 0;
    }
}
