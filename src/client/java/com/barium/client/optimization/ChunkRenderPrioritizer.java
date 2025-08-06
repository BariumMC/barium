package com.barium.client.optimization;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChunkRenderPrioritizer {
    
    /**
     * Calcula uma pontuação para um chunk para determinar sua prioridade de reconstrução.
     * Chunks com PONTUAÇÕES MENORES são mais importantes e serão carregados primeiro.
     *
     * @param cameraPos Posição da câmera do jogador.
     * @param viewVector Vetor de visão da câmera (para onde o jogador está olhando).
     * @param chunk O chunk a ser pontuado.
     * @return A pontuação de prioridade do chunk.
     */
    public static double calculateScore(Vec3d cameraPos, Vec3d viewVector, ChunkBuilder.BuiltChunk chunk) {
        BlockPos origin = chunk.getOrigin();
        Vec3d chunkCenter = new Vec3d(origin.getX() + 8.0, origin.getY() + 8.0, origin.getZ() + 8.0);
        
        // Vetor da câmera para o centro do chunk, normalizado.
        Vec3d vectorToChunk = chunkCenter.subtract(cameraPos).normalize();
        
        // A distância ao quadrado é mais rápida de calcular do que a raiz quadrada.
        double distanceSq = cameraPos.squaredDistanceTo(chunkCenter);
        
        // Produto escalar (Dot Product) entre o vetor de visão e o vetor para o chunk.
        // - Valor perto de 1.0: O chunk está bem no centro da visão.
        // - Valor perto de 0.0: O chunk está a 90 graus (na sua lateral).
        // - Valor perto de -1.0: O chunk está atrás de você.
        double dotProduct = viewVector.dotProduct(vectorToChunk);

        // A FÓRMULA DE PRIORIDADE:
        // A pontuação base é a distância. Chunks mais próximos são melhores.
        // Nós dividimos a distância por um fator baseado no ângulo.
        // Se o chunk está na sua frente (dotProduct alto), a pontuação de distância é drasticamente reduzida,
        // dando a ele uma prioridade muito maior do que um chunk na mesma distância, mas que está atrás de você.
        // O "+ 1.1" é para evitar divisão por zero e para dar um peso extra aos chunks que estão bem no centro.
        return distanceSq / (dotProduct + 1.1);
    }
}