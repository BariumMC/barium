// --- Crie este arquivo em: src/client/java/com/barium/client/optimization/ChunkRenderPrioritizer.java ---
package com.barium.client.optimization;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Gerencia a priorização da construção de chunks com base na proximidade com o jogador.
 * Mantém a posição da câmera atualizada e fornece um método de comparação para
 * ordenar chunks, garantindo que o trabalho mais importante (chunks visíveis)
 * seja feito primeiro.
 */
public class ChunkRenderPrioritizer {

    private static Vec3d cameraPosition = Vec3d.ZERO;

    /**
     * Atualiza a posição da câmera que será usada para os cálculos de prioridade.
     * Deve ser chamado uma vez por frame.
     * @param newPosition A nova posição da câmera.
     */
    public static void updateCameraPosition(Vec3d newPosition) {
        cameraPosition = newPosition;
    }

    /**
     * Compara dois chunks com base em sua distância da última posição conhecida da câmera.
     * Usa distância ao quadrado para evitar o custo computacional da raiz quadrada.
     *
     * @param chunkA O primeiro chunk para comparar.
     * @param chunkB O segundo chunk para comparar.
     * @return -1 se chunkA for mais próximo, 1 se chunkB for mais próximo, 0 se forem iguais.
     */
    public static int compare(ChunkBuilder.BuiltChunk chunkA, ChunkBuilder.BuiltChunk chunkB) {
        BlockPos originA = chunkA.getOrigin();
        BlockPos originB = chunkB.getOrigin();

        double distSqA = cameraPosition.squaredDistanceTo(originA.getX(), originA.getY(), originA.getZ());
        double distSqB = cameraPosition.squaredDistanceTo(originB.getX(), originB.getY(), originB.getZ());

        return Double.compare(distSqA, distSqB);
    }
}