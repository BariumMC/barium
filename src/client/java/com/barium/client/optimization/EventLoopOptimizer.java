package com.barium.client.optimization;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class EventLoopOptimizer {

    private static long lastFrameTime = 0;

    /**
     * Substitui o loop de espera ocupada (busy-wait) do Minecraft por uma espera com sleep inteligente.
     * Isso reduz drasticamente o uso da CPU quando o framerate está limitado, sem sacrificar a responsividade.
     */
    public static void waitForNextFrame() {
        long currentTime = System.nanoTime();
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        double targetFrameTimeNanos = 1_000_000_000.0 / client.options.getMaxFps().getValue();
        long timeSinceLastFrame = currentTime - lastFrameTime;
        
        // Calcula quanto tempo precisamos esperar até o próximo frame.
        double timeToWaitNanos = targetFrameTimeNanos - timeSinceLastFrame;

        // Se ainda temos tempo de sobra, colocamos o thread para dormir.
        if (timeToWaitNanos > 0) {
            try {
                // Converte nanossegundos para segundos (o formato que a função espera).
                // glfwWaitEventsTimeout é a chave: ele dorme, mas acorda instantaneamente para eventos.
                GLFW.glfwWaitEventsTimeout(timeToWaitNanos / 1_000_000_000.0);
            } catch (Exception e) {
                // Em caso de erro, apenas continua.
            }
        }
        
        // Atualiza o tempo do frame para o próximo ciclo.
        lastFrameTime = System.nanoTime();
    }
}