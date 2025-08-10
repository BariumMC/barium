package com.barium.client;

import com.barium.BariumMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BariumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BariumMod.LOGGER.info("Barium Client está inicializando.");
        // A inicialização do Render Manager agora é feita sob demanda (lazy)
        // para evitar problemas de contexto do OpenGL.
    }
}