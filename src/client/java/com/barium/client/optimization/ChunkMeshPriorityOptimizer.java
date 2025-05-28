package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Otimiza a prioridade de geração de meshes de chunk (Chunk Mesh Generation).
 * Usa um sistema adaptativo baseado na direção do olhar do jogador.
 * Baseado nos mappings Yarn 1.21.5+build.1
 * Corrigido: Removido acesso direto ao campo privado sectionPos e substituído método getRotationVector.
 */
public class ChunkMeshPriorityOptimizer {

    // Fator de peso para chunks na direção do olhar
    private static final double LOOK_DIRECTION_WEIGHT = 1.5;
    // Fator de peso para chunks próximos
    private static final double DISTANCE_WEIGHT = 1.0;

    /**
     * Calcula a prioridade de reconstrução para um BuiltChunk com base na distância e direção do olhar.
     *
     * @param builtChunk O chunk compilado (seção).
     * @param camera A câmera do jogador.
     * @return Um valor de prioridade (menor é mais prioritário).
     */
    public static double calculateRebuildPriority(ChunkBuilder.BuiltChunk builtChunk, Camera camera) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.ADAPTIVE_CHUNK_PRIORITY) {
            // Retorna prioridade padrão baseada apenas na distância se desligado
            // Não podemos acessar builtChunk.sectionPos diretamente, então usamos getOrigin() que é público
            BlockPos origin = builtChunk.getOrigin();
            Vec3d centerPos = new Vec3d(origin.getX() + 8, origin.getY() + 8, origin.getZ() + 8);
            return camera.getPos().squaredDistanceTo(centerPos);
        }

        // Usa getOrigin() que é um método público em vez de acessar sectionPos diretamente
        BlockPos origin = builtChunk.getOrigin();
        Vec3d cameraPos = camera.getPos();
        Vec3d centerPos = new Vec3d(origin.getX() + 8, origin.getY() + 8, origin.getZ() + 8);

        // 1. Calcula a distância
        double distanceSq = cameraPos.squaredDistanceTo(centerPos);
        double distancePriority = distanceSq * DISTANCE_WEIGHT;

        // 2. Calcula o fator de direção do olhar
        // getRotationVector() não existe, usamos Vec3d.fromPolar que é o método correto em 1.21.5
        Vec3d lookVector = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        Vec3d chunkDirection = centerPos.subtract(cameraPos).normalize();
        double dotProduct = lookVector.dotProduct(chunkDirection);

        // O dotProduct varia de -1 (oposto) a 1 (mesma direção).
        // Queremos maior prioridade (menor valor) para chunks na direção do olhar (dotProduct próximo a 1).
        // Mapeia o dotProduct para um fator de peso (ex: 1.0 para -1, LOOK_DIRECTION_WEIGHT para 1)
        double lookWeight = MathHelper.lerp((dotProduct + 1.0) / 2.0, 1.0 / LOOK_DIRECTION_WEIGHT, 1.0); // Inverte o peso

        // 3. Combina as prioridades
        // Prioridade final = distância * peso_distância / peso_direção (ou algo similar)
        double finalPriority = distancePriority * lookWeight;

        return finalPriority;
    }

    /**
     * Cria um comparador para a fila de prioridade de reconstrução de chunks.
     *
     * @param camera A câmera.
     * @return Um Comparator para ChunkBuilder.BuiltChunk.
     */
    public static Comparator<ChunkBuilder.BuiltChunk> createPriorityComparator(Camera camera) {
        return Comparator.comparingDouble(chunk -> calculateRebuildPriority(chunk, camera));
    }

    /**
     * Reordena uma fila de prioridade existente com base na nova posição/direção da câmera.
     * (Alternativa a criar um novo comparador a cada frame)
     *
     * @param queue A fila de prioridade a ser reordenada.
     * @param camera A câmera.
     */
    public static void resortPriorityQueue(PriorityQueue<ChunkBuilder.BuiltChunk> queue, Camera camera) {
        if (!BariumConfig.ENABLE_ADVANCED_CULLING || !BariumConfig.ADAPTIVE_CHUNK_PRIORITY) {
            return;
        }
        
        // Recriar a fila com o novo comparador é geralmente a forma mais simples
        // A implementação exata depende de como a fila é gerenciada no ChunkBuilder.
        // Comentado para evitar erros de compilação até que a implementação seja refinada
        /*
        List<ChunkBuilder.BuiltChunk> tempList = new ArrayList<>(queue);
        queue.clear();
        queue.addAll(tempList); // Adiciona de volta com o novo comparador implícito
        */
    }
}
