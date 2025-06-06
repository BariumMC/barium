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

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Redireciona a chamada que obtém a lista de entidades de bloco a serem renderizadas.
     * Em vez de obter todas as entidades de bloco do mundo, nós pré-filtramos a lista,
     * retornando apenas aquelas que devem ser renderizadas de acordo com nossa lógica de culling.
     * Isso reduz drasticamente a carga no loop de renderização principal.
     */
    @Redirect(
        method = "renderBlockEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;getBlockEntities()Ljava/lang/Iterable;"
        )
    )
    private Iterable<BlockEntity> barium$cullBlockEntityList(ClientWorld world) {
        // Obter a câmera de forma segura
        Camera camera = this.client.gameRenderer.getCamera();

        // Se a câmera não estiver disponível, retorne a lista original para evitar crashes.
        if (camera == null) {
            return world.getBlockEntities();
        }

        // Filtra a lista de entidades de bloco usando um stream.
        // Isso é mais limpo e eficaz do que iterar e verificar dentro do loop de renderização.
        return StreamSupport.stream(world.getBlockEntities().spliterator(), false)
            .filter(blockEntity -> ChunkOptimizer.shouldRenderBlockEntity(blockEntity, camera))
            .collect(Collectors.toList());
    }
}