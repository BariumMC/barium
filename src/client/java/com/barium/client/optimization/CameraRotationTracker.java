package com.barium.client.optimization;

import net.minecraft.client.MinecraftClient;

public class CameraRotationTracker {
    private static float lastYaw = 0;
    private static float lastPitch = 0;
    private static boolean isRotatingFast = false;

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();

        float deltaYaw = Math.abs(currentYaw - lastYaw);
        float deltaPitch = Math.abs(currentPitch - lastPitch);

        // Corrige o delta se o yaw cruzar 360/0 graus
        if (deltaYaw > 180.0f) {
            deltaYaw = 360.0f - deltaYaw;
        }

        // Se a mudança for maior que o limite configurado (ex: 2.5 graus por frame), estamos girando rápido
        double threshold = com.barium.config.BariumConfig.C.ROTATION_THRESHOLD;
        isRotatingFast = (deltaYaw > threshold || deltaPitch > threshold);

        lastYaw = currentYaw;
        lastPitch = currentPitch;
    }

    public static boolean isRotatingFast() {
        return com.barium.config.BariumConfig.C.ENABLE_ROTATION_THROTTLING && isRotatingFast;
    }
}