package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza a renderização de entidades no cliente, aplicando culling e Level of Detail (LOD)
 * com base na distância e tipo de entidade.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
public class EntityRenderingOptimizer {

    // Distância máxima para renderizar entidades (culling)
    private static final double MAX_RENDER_DISTANCE_SQ = BariumConfig.ENTITY_RENDER_MAX_DISTANCE * BariumConfig.ENTITY_RENDER_MAX_DISTANCE;
    // Distância para iniciar a renderização em LOD mais baixa (médio)
    private static final double LOD_DISTANCE_MEDIUM_SQ = BariumConfig.ENTITY_RENDER_LOD_DISTANCE_MEDIUM * BariumConfig.ENTITY_RENDER_LOD_DISTANCE_MEDIUM;
    // Distância para iniciar a renderização em LOD ainda mais baixa (alto, significa mais longe)
    private static final double LOD_DISTANCE_HIGH_SQ = BariumConfig.ENTITY_RENDER_LOD_DISTANCE_HIGH * BariumConfig.ENTITY_RENDER_LOD_DISTANCE_HIGH;

    /**
     * Níveis de detalhe para renderização de entidades.
     */
    public enum RenderLevel {
        FULL,      // Renderizar com detalhes completos
        SIMPLIFIED, // Renderizar com modelo simplificado (se implementado)
        CULLED     // Não renderizar
    }

    public static void init() {
        BariumMod.LOGGER.info("Inicializando EntityRenderingOptimizer");
    }

    /**
     * Determina o nível de renderização para uma entidade específica.
     *
     * @param entity A entidade a ser renderizada.
     * @param camera A câmera do jogo.
     * @return O RenderLevel apropriado.
     */
    public static RenderLevel getEntityRenderLevel(Entity entity, Camera camera) {
        if (!BariumConfig.ENABLE_ENTITY_RENDERING_OPTIMIZATION) {
            return RenderLevel.FULL; // Otimização desativada, sempre renderiza full
        }

        // Não otimizar jogadores (exceto o próprio jogador, que é tratado separadamente)
        // ou entidades muito importantes que precisam de renderização constante.
        if (entity instanceof PlayerEntity && entity != MinecraftClient.getInstance().player) {
            return RenderLevel.FULL;
        }

        Vec3d cameraPos = camera.getPos();
        Vec3d entityPos = entity.getPos();
        double distanceSq = cameraPos.squaredDistanceTo(entityPos);

        // 1. Culling por distância máxima
        if (distanceSq > MAX_RENDER_DISTANCE_SQ) {
            return RenderLevel.CULLED;
        }

        // 2. LOD por distância
        if (BariumConfig.ENABLE_ENTITY_RENDER_LOD) {
            if (distanceSq > LOD_DISTANCE_HIGH_SQ) {
                // Muito distante, mas ainda dentro da distância máxima de renderização
                // Poderíamos ter uma opção para pular renderização de entidades "insignificantes"
                // ou muito pequenas aqui. Por enquanto, simplificamos o modelo.
                return RenderLevel.SIMPLIFIED;
            } else if (distanceSq > LOD_DISTANCE_MEDIUM_SQ) {
                // Distância média, modelo menos detalhado
                return RenderLevel.SIMPLIFIED;
            }
        }

        // Por padrão ou se estiver perto, renderiza com detalhes completos
        return RenderLevel.FULL;
    }

    // TODO: A implementação de modelos simplificados ou shaders de LOD é complexa e requer
    // mudanças no sistema de renderização de modelos do Minecraft. Para esta versão,
    // 'SIMPLIFIED' atua como um marcador para futuras implementações, ou pode ser usado
    // para um simples "frame-skip" no mixin (que pode causar stuttering).
}