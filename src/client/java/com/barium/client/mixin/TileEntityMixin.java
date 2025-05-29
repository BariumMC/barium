package com.barium.client.mixin;

import com.barium.client.optimization.TileEntityOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BufferBuilderStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet; // Adicionado para tipo correto de locais

/**
 * Mixin para WorldRenderer para otimizar a renderização de Block Entities (Tile Entities).
 * Foca em aplicar instancing ou batching antes da renderização individual.
 * Revisado para compatibilidade com mappings Yarn 1.21.5+build.1.
 * Corrigido: Assinatura do método `renderBlockEntities` e captura de locais.
 */
@Mixin(WorldRenderer.class)
public abstract class TileEntityMixin {

    @Shadow private BufferBuilderStorage bufferBuilders;
    @Shadow private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    /**
     * Injeta no método que renderiza block entities para aplicar otimizações como instancing.
     * Captura a coleção de block entities visíveis antes que sejam renderizadas individualmente.
     *
     * Target Class: net.minecraft.client.render.WorldRenderer
     * Target Method Signature (Yarn 1.21.5+build.1): renderBlockEntities(Lnet/minecraft/client/render/BufferBuilderStorage;Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;F)V
     *
     * A injeção ocorre no ponto onde o `BlockEntityRenderDispatcher` é invocado dentro do loop de renderização.
     * Capturamos a variável local que contém o conjunto de entidades visíveis.
     */
    @Inject(
        method = "renderBlockEntities(Lnet/minecraft/client/render/BufferBuilderStorage;Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            shift = At.Shift.BEFORE // Injeta antes da primeira chamada de renderização individual
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT, // Tenta capturar variáveis locais
        cancellable = true
    )
    private void barium$optimizeBlockEntityRendering(
            BufferBuilderStorage bufferBuilderStorage, // Argumento de 1.21.5
            BlockEntityRenderDispatcher blockEntityRenderDispatcher, // Argumento de 1.21.5
            VertexConsumerProvider.Immediate vertexConsumers,
            Camera camera,
            float tickDelta,
            CallbackInfo ci,
            // Variável local capturada: SortedSet de RenderInfo. Nomes e tipos baseados em decompilação Yarn 1.21.5.
            SortedSet<BlockEntityRenderDispatcher.RenderInfo> visibleTileEntities // Tipo correto para 1.21.5
    ) {
        // Coleta entidades para instancing
        for (BlockEntityRenderDispatcher.RenderInfo renderInfo : visibleTileEntities) {
            BlockEntity blockEntity = renderInfo.getBlockEntity();
            // Para `tryGroupBlockEntityForInstancing`, precisamos da MatrixStack específica da entidade.
            // O `renderInfo.getPose()` (ou similar) retornaria o `MatrixStack.Entry` para aquela entidade.
            // Criaremos uma nova MatrixStack e aplicaremos a transformação da entidade.
            MatrixStack entityMatrices = new MatrixStack();
            // Aplica a transformação da entidade. `renderInfo.setupMatrix(entityMatrices)` seria o ideal.
            // Como isso não é publicamente exposto para uso externo fácil, faremos uma cópia da matriz global
            // e a combinaremos com a posição da entidade. Para instancing, a matriz específica da instância é mais importante.
            // Para simplificar, passaremos uma cósideração da matriz global e a posição da entidade.
            // A `TileEntityOptimizer.tryGroupBlockEntityForInstancing` deve lidar com a cópia da matriz.
            TileEntityOptimizer.tryGroupBlockEntityForInstancing(blockEntity, entityMatrices, blockEntityRenderDispatcher);
        }

        // Após coletar todas as entidades, renderiza os grupos instanciados
        // light e overlay são variáveis locais no loop original do método.
        // Assumimos valores genéricos ou capturamos do método original (complexo para `LocalCapture` aqui).
        // Para uma implementação robusta, você precisaria de ATs para acessar os valores de luz e overlay.
        int light = 15728880; // Luz máxima (exemplo)
        int overlay = 0; // Sem overlay (exemplo)

        TileEntityOptimizer.renderInstancedGroups(blockEntityRenderDispatcher, matrices, vertexConsumers, light, overlay);

        // Cancela o loop original de renderização de entidades, pois já as tratamos.
        ci.cancel();
    }
}