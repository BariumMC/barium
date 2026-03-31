package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class HandRenderOptimizer {
    private static int frameCounter = 0;
    private static float lastYaw = 0.0f;
    private static float lastPitch = 0.0f;
    private static boolean lastFrameSkipped = false;

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

        if (player.isUsingItem() || player.isBlocking() || player.isRiding()) {
            lastFrameSkipped = false;
            return false;
        }

        // Estratégia adaptativa: só tenta economizar durante rotação rápida + FPS baixo,
        // evitando skip consecutivo para eliminar o efeito de "piscar" perceptível.
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float deltaYaw = Math.abs(yaw - lastYaw);
        float deltaPitch = Math.abs(pitch - lastPitch);
        if (deltaYaw > 180.0f) {
            deltaYaw = 360.0f - deltaYaw;
        }

        lastYaw = yaw;
        lastPitch = pitch;

        float angularDelta = deltaYaw + deltaPitch;
        int fps = Math.max(1, client.getCurrentFps());
        boolean underPressure = fps < 55;
        boolean rotatingFast = angularDelta > 4.0f;

        if (!underPressure || !rotatingFast) {
            lastFrameSkipped = false;
            return false;
        }

        int skipFrames = Math.max(1, BariumConfig.C.HAND_RENDER_SKIP_FRAMES);
        frameCounter = (frameCounter + 1) % (skipFrames + 1);
        boolean shouldSkipByCadence = frameCounter != 0;

        if (!shouldSkipByCadence || lastFrameSkipped) {
            lastFrameSkipped = false;
            return false;
        }

        lastFrameSkipped = true;
        return true;
    }
}
