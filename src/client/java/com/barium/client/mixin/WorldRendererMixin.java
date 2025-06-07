package com.barium.client.mixin;

import com.barium.client.optimization.ChunkOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Redireciona a chamada que obtém a lista de entidades de bloco a serem renderizadas.
     * Em vez de obter todas as entidades de bloco do mundo, nós pré-filtramos a lista,
     * retornando apenas aquelas que devem ser renderizadas de acordo com nossa lógica de culling.
     * Isso é mais eficiente e evita a chamada ao método que estava causando erro de compilação.
     */
    @Redirect(
        method = "renderBlockEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;getBlockEntities()Ljava/lang/Iterable;"
        )
    )
    private Iterable<BlockEntity> barium$cullBlockEntityList(ClientWorld world) {
        Camera camera = this.client.gameRenderer.getCamera();

        // Acessa o mapa de entidades de bloco diretamente através de um accessor
        // para evitar a chamada de método problemática e recursão.
        Collection<BlockEntity> allBlockEntities = ((ClientWorldAccessor) world).getBlockEntityMap().values();

        if (camera == null) {
            return allBlockEntities; // Retorna a coleção original se a câmera não estiver pronta
        }

        // Filtra a lista de entidades de bloco usando um stream.
        return allBlockEntities.stream()
            .filter(blockEntity -> ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera))
            .collect(Collectors.toList());
    }
}