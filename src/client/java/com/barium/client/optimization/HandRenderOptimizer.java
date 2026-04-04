package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

public final class HandRenderOptimizer {
    private HandRenderOptimizer() {
    }

    public static boolean shouldSkipHandRender() {
        if (!BariumConfig.C.ENABLE_HAND_RENDER_THROTTLING) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.currentScreen != null) {
            return false;
        }

        if (player.isUsingItem() || player.isBlocking() || player.isRiding() || player.isSpectator()) {
            return false;
        }

        // Política anti-flicker: nunca pular mão em foco.
        // Só economiza quando a janela não está ativa/minimizada.
        if (!client.isWindowFocused()) {
            return true;
        }

        long handle = client.getWindow().getHandle();
        return handle != 0L && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }
}
