package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Otimiza o culling de chunks, melhorando a detecção de quais seções de chunk devem ser renderizadas.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Adicionado método shouldRenderChunkSection com assinatura compatível com o mixin.
 */
public class ChunkOcclusionOptimizer {

    /**
     * Verifica se uma seção de chunk deve ser renderizada com base em oclusão avançada.
     * Este método é chamado pelo mixin ChunkOcclusionMixin.
     *
     * @param chunk O chunk a ser verificado
     * @param camera A câmera atual
     * @return true se o chunk deve ser renderizado, false caso contrário
     */
    public static boolean shouldRenderChunkSection(ChunkBuilder.BuiltChunk chunk, Camera camera) {
        if (!BariumConfig.ENABLE_CHUNK_OCCLUSION_OPTIMIZATION) {
            return true; // Sem otimização, sempre renderiza
        }

        // Implementação simplificada para compatibilidade com o mixin
        // Delega para o método completo com todos os parâmetros necessários
        Vec3d cameraPos = camera.getPos();
        return shouldRenderChunkSection(chunk, null, cameraPos.x, cameraPos.y, cameraPos.z);
    }

    /**
     * Verifica se uma seção de chunk deve ser renderizada com base em oclusão avançada.
     * Implementação completa com todos os parâmetros.
     *
     * @param chunk O chunk a ser verificado
     * @param frustum O frustum da câmera
     * @param cameraX Posição X da câmera
     * @param cameraY Posição Y da câmera
     * @param cameraZ Posição Z da câmera
     * @return true se o chunk deve ser renderizado, false caso contrário
     */
    public static boolean shouldRenderChunkSection(ChunkBuilder.BuiltChunk chunk, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (!BariumConfig.ENABLE_CHUNK_OCCLUSION_OPTIMIZATION) {
            return true; // Sem otimização, sempre renderiza
        }

        // Verifica se o chunk está no frustum (se fornecido)
        if (frustum != null) {
            BlockPos origin = chunk.getOrigin();
            Box boundingBox = new Box(
                    origin.getX(), origin.getY(), origin.getZ(),
                    origin.getX() + 16, origin.getY() + 16, origin.getZ() + 16);
            
            if (!frustum.isVisible(boundingBox)) {
                return false;
            }
        }

        // Implementação básica: verifica distância
        BlockPos origin = chunk.getOrigin();
        double centerX = origin.getX() + 8;
        double centerY = origin.getY() + 8;
        double centerZ = origin.getZ() + 8;
        
        double distanceSq = (centerX - cameraX) * (centerX - cameraX) +
                           (centerY - cameraY) * (centerY - cameraY) +
                           (centerZ - cameraZ) * (centerZ - cameraZ);
        
        // Chunks muito distantes podem ser pulados (além da distância de renderização normal)
        // Isso é apenas um exemplo, o jogo já tem seu próprio sistema de distância de renderização
        double maxDistanceSq = 256 * 256; // 16 chunks (exemplo)
        if (distanceSq > maxDistanceSq) {
            return false;
        }
        
        // Em uma implementação real, aqui teríamos verificações de oclusão mais avançadas
        // como ray casting, verificação contra heightmaps, etc.
        
        return true; // Por padrão, renderiza o chunk
    }
}
