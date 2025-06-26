package com.barium.client.mixin.render;

import com.barium.client.render.region.RegionRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ESTE É UM MIXIN DE SUBSTITUIÇÃO COMPLETA.
 * Ele "hijacks" o WorldRenderer do Minecraft para usar nosso próprio sistema de RenderRegion.
 * Este é um exemplo arquitetônico e não funcionará sem uma implementação completa do sistema de região.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements AutoCloseable {

    // Injetamos uma instância do nosso próprio renderizador.
    private RegionRenderer barium_regionRenderer;

    // Usamos @Shadow para obter acesso a campos privados da classe original.
    @Shadow @Final private MinecraftClient client;
    @Shadow private World world;

    /**
     * Injetamos no final do construtor do WorldRenderer.
     * Assim que o WorldRenderer vanilla é criado, nós criamos nosso RegionRenderer.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void barium_onInit(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        // Assume que o mundo não é nulo aqui, mas uma checagem seria boa.
        this.barium_regionRenderer = new RegionRenderer(this.world);
    }

    /**
     * Injetamos quando o mundo é trocado (ex: entrar num servidor, mudar de dimensão).
     * É crucial limpar os recursos do mundo antigo e criar um novo renderer para o mundo novo.
     */
    @Inject(method = "setWorld", at = @At("RETURN"))
    private void barium_onSetWorld(World world, CallbackInfo ci) {
        if (this.barium_regionRenderer != null) {
            this.barium_regionRenderer.dispose(); // Libera VBOs e outros recursos da GPU
        }
        this.barium_regionRenderer = new RegionRenderer(world);
    }

    /**
     * HIJACK 1: Substituição da lógica de atualização do terreno.
     * O método setupTerrain decide quais chunks reconstruir. Nós o cancelamos e
     * chamamos a lógica de atualização do nosso RegionRenderer.
     */
    @Inject(method = "setupTerrain", at = @At("HEAD"), cancellable = true)
    private void barium_hijackSetupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if (this.barium_regionRenderer == null) return;
        
        this.barium_regionRenderer.update(camera, frustum);
        ci.cancel(); // IMPEDE a execução do método setupTerrain original.
    }

    /**
     * HIJACK 2: Substituição da lógica de renderização.
     * O método renderLayer desenha os chunks. Nós o cancelamos e
     * chamamos a lógica de renderização do nosso RegionRenderer.
     */
    @Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
    private void barium_hijackRenderLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        if (this.barium_regionRenderer == null) return;

        // Prepara o estado do OpenGL para nossa renderização
        RenderSystem.assertOnRenderThread();
        renderLayer.startDrawing();

        this.barium_regionRenderer.render(renderLayer, matrices, cameraX, cameraY, cameraZ);

        // Finaliza o estado do OpenGL
        renderLayer.endDrawing();
        ci.cancel(); // IMPEDE a execução do método renderLayer original.
    }

    /**
     * HIJACK 3: Captura de atualizações de blocos.
     * Quando o jogo agenda a reconstrução de um chunk, nós interceptamos para
     * marcar nossa RenderRegion correspondente como "suja".
     */
    @Inject(method = "scheduleBlockRerender", at = @At("HEAD"), cancellable = true)
    private void barium_onBlockUpdate(int x, int y, int z, CallbackInfo ci) {
        if (this.barium_regionRenderer == null) return;

        this.barium_regionRenderer.onBlockUpdate(new BlockPos(x, y, z));
        ci.cancel(); // Não precisamos que o vanilla faça nada.
    }

    /**
     * Injetamos no método de limpeza para garantir que nossos recursos da GPU sejam liberados
     * quando o jogo for fechado.
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void barium_onClose(CallbackInfo ci) {
        if (this.barium_regionRenderer != null) {
            this.barium_regionRenderer.dispose();
        }
    }
}