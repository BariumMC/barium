package com.barium.client;

import com.barium.BariumMod;
import com.barium.client.optimization.HudOptimizer;
import com.barium.client.optimization.ParticleOptimizer;
import com.barium.client.optimization.ChunkOptimizer;
import com.barium.client.util.ChunkRenderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {

    private final ChunkRenderManager chunkRenderManager = new ChunkRenderManager();

    @Override
    public void onInitializeClient() {
        BariumMod.LOGGER.info("Inicializando cliente Barium");
        
        // Inicialização dos otimizadores client-side
        HudOptimizer.init();
        ParticleOptimizer.init();
        ChunkOptimizer.init();

        // Registra o evento de tick do cliente para atualizar o ChunkRenderManager
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        // Obtém a posição e rotação do jogador
        Vec3d playerPos = client.player.getPos();
        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();

        // Verifica se o cálculo de chunks visíveis precisa ser refeito
        if (chunkRenderManager.shouldRecalculate(playerPos, yaw, pitch)) {
            chunkRenderManager.calculateChunksToRender(client, playerPos, yaw, pitch);
        }
    }
}