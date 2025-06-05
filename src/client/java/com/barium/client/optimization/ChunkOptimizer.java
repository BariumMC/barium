package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
// import net.minecraft.client.render.Frustum; // Removido, pois getFrustum() não está disponível
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza a renderização de chunks e entidades de bloco.
 * Foca em culling de entidades de bloco para melhorar a performance.
 */
public class ChunkOptimizer {

    /**
     * Inicializa o ChunkOptimizer.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando ChunkOptimizer");
        // Não há caches complexos para limpar aqui por enquanto.
    }

    /**
     * Determina se uma entidade de bloco deve ser renderizada.
     * Aplica culling por distância e frustum.
     * @param blockEntity A entidade de bloco a ser verificada.
     * @param camera A câmera atual do jogador.
     * @return true se a entidade de bloco deve ser renderizada, false caso contrário.
     */
    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, Camera camera) {
        if (!BariumConfig.ENABLE_CHUNK_OPTIMIZATION || !BariumConfig.ENABLE_BLOCK_ENTITY_CULLING) {
            return true; // Se as otimizações estiverem desativadas, sempre renderiza
        }

        Vec3d blockEntityPos = Vec3d.ofCenter(blockEntity.getPos());
        Vec3d cameraPos = camera.getPos();

        double distanceSq = blockEntityPos.squaredDistanceTo(cameraPos);

        // Culling por distância
        if (distanceSq > BariumConfig.MAX_BLOCK_ENTITY_RENDER_DISTANCE_SQ) {
            return false;
        }

        // Culling por Frustum (visível na tela) foi temporariamente desativado
        // devido à indisponibilidade do método getFrustum() na classe Camera.
        // Para reativar esta otimização, será necessário encontrar o método correto
        // para verificar a visibilidade da frustum na versão 1.21.5 do Minecraft.
        
        return true; // Retorna sempre true se passar pela checagem de distância
    }

    // Comentários sobre outras otimizações de chunk (para referência futura):
    /*
     * Outras otimizações de chunk (mais complexas e sensíveis a conflitos com Sodium):
     *
     * 1. Frustum Culling para Seções de Chunk:
     * O Minecraft e o Sodium já fazem um excelente trabalho nisso. Tentar reimplementar
     * isso pode causar conflitos ou ser redundante. Envolve injetar na lógica de
     * WorldRenderer ou ChunkRenderer.
     *
     * 2. Occlusion Culling:
     * Não renderizar blocos que estão completamente escondidos por outros blocos opacos.
     * Extremamente complexo de implementar de forma eficiente e sem bugs. Geralmente
     * requer um sistema de oclusão personalizado.
     *
     * 3. Deferred Chunk Meshing/Uploading:
     * Gerar e enviar meshes de chunks para a GPU em um thread separado ou de forma
     * assíncrona para evitar picos de FPS (stuttering) ao carregar novos chunks.
     * Sodium faz isso muito bem.
     *
     * 4. Dynamic LOD para Chunks Distantes:
     * Renderizar chunks mais distantes com uma qualidade visual reduzida (menos detalhes,
     * meshes mais simples) para economizar recursos. Isso é avançado e difícil.
     */
}
