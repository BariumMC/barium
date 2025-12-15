package com.barium.client.optimization;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChunkRenderPrioritizer {
    
    /**
     * Calcula a prioridade de renderização. Menor valor = Maior prioridade.
     */
    public static double calculateScore(Vec3d cameraPos, Vec3d viewVector, ChunkBuilder.BuiltChunk chunk) {
        BlockPos origin = chunk.getOrigin();
        
        // Usa coordenadas relativas diretas (double) para evitar alocação de Vec3d
        double relX = (origin.getX() + 8.0) - cameraPos.x;
        double relY = (origin.getY() + 8.0) - cameraPos.y;
        double relZ = (origin.getZ() + 8.0) - cameraPos.z;
        
        // Distância ao quadrado (evita Math.sqrt)
        double distSq = relX * relX + relY * relY + relZ * relZ;
        
        // Se estiver muito perto (dentro de 1 chunk), prioridade máxima imediata
        if (distSq < 256.0) {
            return -1000.0;
        }

        // Normalização aproximada para o Dot Product
        // Math.invSqrt é caro em Java puro, mas Math.sqrt é intrinsificado.
        // Ainda assim, só precisamos do sinal e magnitude relativa.
        double len = Math.sqrt(distSq);
        double normX = relX / len;
        double normY = relY / len;
        double normZ = relZ / len;

        // Dot Product: quão alinhado está com a visão?
        double dot = viewVector.x * normX + viewVector.y * normY + viewVector.z * normZ;

        // Lógica de Foco:
        // Se dot > 0.8 (está na frente), reduzimos o "custo" da distância artificialmente.
        // Se dot < 0 (está atrás), penalizamos severamente.
        double priorityBias = (dot > 0) ? (1.0 - dot) * 0.5 : 2.0 - dot;
        
        // Score final: Distância ponderada pelo ângulo.
        // Chunks na mira carregam primeiro, depois os periféricos, depois os de trás.
        double distanceMultiplier = 1.0;
        if (com.barium.config.BariumConfig.C.ENABLE_LLVMPIPE_MODE) {
            // Em software renderer, penalizamos mais chunks distantes para manter o foco
            // em uploads locais e preservar FPS quando MAX_CHUNK_UPLOADS_PER_FRAME é alto.
            distanceMultiplier = 1.0 + Math.min(2.0, (double)com.barium.config.BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME / 8.0);
        }

        return distSq * priorityBias * distanceMultiplier;
    }
}