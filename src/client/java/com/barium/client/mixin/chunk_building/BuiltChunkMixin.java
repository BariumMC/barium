package com.barium.client.mixin.chunk_building;

import com.barium.BariumMod;
import com.barium.client.mixin.accesor.BakedQuadAccessor;
import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class BuiltChunkMixin {

    // Shadowed fields are needed to access internal state of BuiltChunk
    @Shadow @Final private ChunkBuilder.ChunkData chunkData;
    @Shadow @Final private BlockPos origin;
    @Shadow @Final private ChunkBuilder chunkBuilder;
    @Shadow protected volatile Set<RenderLayer> nonEmptyLayers;
    @Shadow private MatrixStack.Entry matrixEntry;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int z;

    // --- Conceito de Quad Sorting para Batching ---
    // Este é um Mixin complexo que exigiria uma reescrita significativa do método `rebuild`
    // ou de uma parte crítica dele. A abordagem mais comum é substituir a lógica de
    // `BlockRenderManager.renderBlock` que é chamada em um loop para cada bloco no chunk.

    // A ideia seria:
    // 1. Desabilitar a chamada original de `BlockRenderManager.renderBlock` via `@Inject(cancellable=true)`
    //    ou `@Overwrite`.
    // 2. Coletar todos os `BakedQuad`s para cada `RenderLayer` em listas separadas.
    // 3. Para cada `RenderLayer`, classificar as listas de quads usando o `Sprite` como critério.
    // 4. Iterar as quads sorteadas e adicionar manualmente ao `BufferBuilder` (VertexConsumer).

    // Devido à complexidade e ao boilerplate de recriar a lógica de renderização de blocos
    // dentro de um Mixin, um exemplo completo seria muito extenso.
    // Abaixo está uma *ilustração* de onde e como a otimização de sorting se encaixaria,
    // mas a implementação real exigiria um grande refactor.

    @Inject(method = "rebuild", at = @At("HEAD"), cancellable = true)
    private void barium$optimizeChunkRebuild(float x, float y, float z, ChunkBuilder.ChunkRenderTask chunkRenderTask, CallbackInfo ci) {
        if (!BariumConfig.get().chunkBuilding.enableQuadSorting) {
            return; // Se a otimização não estiver ativa, deixe o vanilla agir.
        }

        // --- Inicio da Logica de Sobrescrita ou Filtragem ---
        // Aqui você precisaria re-implementar a maior parte da lógica de `rebuild`.
        // Isso normalmente envolve:
        // 1. Obtendo o mundo e o RenderManager.
        // 2. Iterando sobre cada BlockPos no chunk.
        // 3. Para cada BlockPos, obtendo o BlockState e o BakedModel.
        // 4. Para cada BakedModel, obtendo todas as BakedQuads (direcionais e não direcionais).
        // 5. Em vez de chamar `BlockRenderManager.renderBlock`, você coletaria as quads.

        // Exemplo conceitual de coleta e ordenação (aqui só para ilustrar, NÃO funcional por si só):
        Map<RenderLayer, List<BakedQuad>> quadsByRenderLayer = new EnumMap<>(RenderLayer.class);

        // Imagine que este loop coleta as quads (substituiria o loop de renderização vanilla)
        // for (BlockPos localPos : BlockPos.iterate(BlockPos.ORIGIN, new BlockPos(15, 15, 15))) {
        //     BlockPos worldPos = this.origin.add(localPos);
        //     BlockState blockState = chunkRenderTask.world.getBlockState(worldPos);
        //     BakedModel model = chunkRenderTask.blockRenderManager.getModel(blockState);
        //     // ... lógica para obter quads para todas as faces e não direcionais
        //     // ... adicionar quads para o mapa quadsByRenderLayer de acordo com o RenderLayer apropriado
        // }

        // Depois de coletar todas as quads:
        // Para cada RenderLayer, classifique os quads por sprite (textura)
        for (RenderLayer layer : quadsByRenderLayer.keySet()) {
            List<BakedQuad> quads = quadsByRenderLayer.get(layer);
            if (quads != null && !quads.isEmpty()) {
                quads.sort(Comparator.comparing(quad -> ((BakedQuadAccessor) quad).getSprite().getId().toString())); // Sort by texture ID
                // Agora, adicione estas quads sorteadas ao BufferBuilder para o RenderLayer correspondente.
                // Isso envolveria obter o VertexConsumer para o layer e chamar `quad.consume(vertexConsumer, ...)`
                // ou `vertexConsumer.quad(quad, ...)` para cada quad.
                // Isso é onde a redução real de draw calls aconteceria, ao agrupar
                // vértices com a mesma textura antes de enviá-los.
            }
        }
        // Este `ci.cancel()` faria com que o método original não fosse executado,
        // mas você precisaria ter re-implementado toda a lógica do `rebuild` acima.
        // ci.cancel();
        BariumMod.LOGGER.warn("Chunk sorting optimization is conceptually enabled but not fully implemented due to complexity.");
    }
}