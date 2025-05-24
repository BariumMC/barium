package com.barium.client;

import com.barium.BariumMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents; // Import para eventos de desconexão

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BariumMod.LOGGER.info("Inicializando cliente Barium");

        // Registrar eventos para limpar caches em situações apropriadas
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            BariumMod.LOGGER.debug("Caches de cliente limpos devido a desconexão.");
        });

        BariumMod.LOGGER.info("Cliente Barium inicializado com sucesso!");
    }
}