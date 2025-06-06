package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import com.google.common.collect.Lists;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow private ClientWorld world;

    /**
     * Redireciona a chamada para obter a lista de entidades de bloco do mundo.
     * Em vez de retornar todas, pré-filtramos a lista usando nossas otimizações de culling.
     * Isso reduz drasticamente o número de entidades que o loop de renderização principal precisa processar.
     */
    @Redirect(
        method = "renderBlockEntities",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
        )
    )
    private Object barium$cullBlockEntitiesBeforeRender(Iterator<BlockEntity> iterator) {
        // Este é um truque para obter acesso à câmera, que não é um argumento do método
        // mas é um campo acessível no contexto do WorldRenderer.
        Camera camera = ((WorldRendererAccessor) this).getCamera();

        BlockEntity blockEntity = iterator.next();

        // Usa a lógica de culling existente. Se não deve renderizar, retorna null.
        // O loop em renderBlockEntities tem uma verificação de nulidade, então isso efetivamente o ignora.
        // NOTA: Esta abordagem é menos ideal que filtrar a lista inteira, mas é mais simples de injetar.
        // Uma abordagem melhor seria modificar a variável local 'iterator', mas isso é mais complexo.
        if (ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
            return blockEntity;
        }

        // Retorna um nulo "falso" para o loop que o chamou. O código do Minecraft vai pular a renderização.
        // Como o iterador original ainda avançou, precisamos encontrar a próxima entidade válida.
        while (iterator.hasNext()) {
            blockEntity = iterator.next();
            if (ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera)) {
                return blockEntity;
            }
        }

        // Se chegarmos aqui, nenhuma outra entidade no iterador é visível.
        // Retornar o último valor (que sabemos que não é renderizável) é seguro,
        // pois a verificação hasNext() no loop principal falhará na próxima iteração.
        return blockEntity;
    }

    // Acessador para obter a câmera de dentro do WorldRenderer
    @Redirect(
        method = "renderBlockEntities",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Iterator;hasNext()Z"
        )
    )
    private boolean barium$checkNextVisible(Iterator<BlockEntity> iterator) {
        // Precisamos verificar se há um próximo item *visível*, não apenas se há um próximo item.
        // Infelizmente, sem reescrever todo o método, isso se torna complexo.
        // A abordagem de retornar nulo no redirect do `next()` é mais simples e eficaz.
        return iterator.hasNext(); // Manter o comportamento original aqui por enquanto.
    }
}