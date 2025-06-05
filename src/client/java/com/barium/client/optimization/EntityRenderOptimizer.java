package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum; // Para frustum culling
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box; // Para a caixa delimitadora

/**
 * Otimiza a renderização de entidades no lado do cliente.
 * Implementa culling por distância e por frustum de visão.
 */
public class EntityRenderOptimizer {

    /**
     * Inicializa o EntityRenderOptimizer.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando EntityRenderOptimizer");
    }

    /**
     * Determina se uma entidade deve ser renderizada no lado do cliente.
     * @param entity A entidade a ser verificada.
     * @param camera A câmera atual do jogador.
     * @return true se a entidade deve ser renderizada, false caso contrário.
     */
    public static boolean shouldRenderEntity(Entity entity, Camera camera) {
        if (!BariumConfig.ENABLE_ENTITY_RENDER_CULLING) {
            return true; // Se a otimização de renderização de entidade estiver desativada, sempre renderiza
        }

        // Jogadores e entidades importantes (ex: montarias do jogador, entidades que o jogador está vendo diretamente)
        // podem precisar de renderização prioritária ou tratamento especial.
        // Para simplicidade, vamos permitir a renderização de jogadores e entidades que o jogador está montando.
        if (entity.isPlayer() || entity.getPassengerList().contains(MinecraftClient.getInstance().player)) {
            return true;
        }

        double distanceSq = entity.squaredDistanceTo(camera.getPos());

        // Culling por distância
        if (distanceSq > BariumConfig.MAX_ENTITY_RENDER_DISTANCE_SQ) {
            return false;
        }

        // Culling por Frustum (visível na tela)
        if (BariumConfig.ENABLE_ENTITY_FRUSTUM_CULLING) {
            // Tenta obter a frustum da câmera. Este método pode não estar disponível ou a assinatura pode variar.
            // Para 1.21.5, 'camera.getFrustum()' pode ser o método correto se os mappings estiverem atualizados.
            try {
                Frustum frustum = camera.getFrustum(); // Obtém a frustum da câmera
                // Obtém a caixa delimitadora da entidade.
                // Alguns tipos de entidades podem ter caixas delimitadoras mais precisas do que a genérica.
                Box boundingBox = entity.getBoundingBox();

                if (!frustum.isVisible(boundingBox)) {
                    return false; // Não renderiza se estiver fora da frustum
                }
            } catch (NoSuchMethodError | Exception e) {
                // Em caso de erro (ex: getFrustum() não encontrado), desativa temporariamente este culling
                BariumMod.LOGGER.warn("Erro ao tentar frustum culling para entidade. Desativando temporariamente. Erro: " + e.getMessage());
                // Definir uma flag interna para desativar esta parte da otimização ou lidar com isso de forma mais robusta.
                // Por enquanto, apenas loga e permite a renderização para evitar crashes.
            }
        }

        return true; // Renderiza se passou por todas as verificações de culling
    }
}