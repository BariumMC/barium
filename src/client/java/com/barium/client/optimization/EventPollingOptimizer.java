package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

/**
 * Agendador adaptativo de pollEvents inspirado em técnicas usadas em mods
 * de performance: reduz trabalho quando o jogo está em background sem
 * sacrificar responsividade ao trocar foco.
 */
public final class EventPollingOptimizer {
    private static final long FAST_UNFOCUSED_INTERVAL_MS = 4L;
    private static final long NORMAL_UNFOCUSED_INTERVAL_MS = 8L;
    private static final long IDLE_UNFOCUSED_INTERVAL_MS = 16L;
    private static final long MENU_UNFOCUSED_INTERVAL_MS = 16L;
    private static final long MENU_IDLE_UNFOCUSED_INTERVAL_MS = 24L;
    private static final long ICONIFIED_INTERVAL_MS = 33L;
    private static final long BACKGROUND_GRACE_MS = 2_000L;
    private static final long BACKGROUND_IDLE_MS = 10_000L;

    private static long lastPollMs = 0L;
    private static long unfocusedSinceMs = -1L;
    private static boolean wasFocused = true;

    private EventPollingOptimizer() {
    }

    public static boolean shouldSkipPollEvents(MinecraftClient client) {
        if (client == null) return false;

        boolean focused = client.isWindowFocused();
        long now = Util.getMeasuringTimeMs();

        if (focused) {
            if (!wasFocused) {
                wasFocused = true;
                unfocusedSinceMs = -1L;
                return false;
            }

            if (!BariumConfig.C.ENABLE_FOCUSED_EVENT_THROTTLING) {
                return false;
            }

            long interval = Math.max(0L, BariumConfig.C.FOCUSED_EVENT_POLL_INTERVAL_MS);
            return now - lastPollMs < interval;
        }

        if (!BariumConfig.C.ENABLE_BACKGROUND_EVENT_THROTTLING) return false;

        if (wasFocused) {
            wasFocused = false;
            unfocusedSinceMs = now;
        }

        long interval = computeTargetInterval(client, now);
        return now - lastPollMs < interval;
    }

    public static void markPollExecuted() {
        lastPollMs = Util.getMeasuringTimeMs();
    }

    private static long computeTargetInterval(MinecraftClient client, long now) {
        if (isWindowIconified(client)) {
            return ICONIFIED_INTERVAL_MS;
        }

        if (client.world == null) {
            long unfocusedFor = unfocusedSinceMs < 0L ? 0L : now - unfocusedSinceMs;
            return unfocusedFor <= BACKGROUND_IDLE_MS
                    ? MENU_UNFOCUSED_INTERVAL_MS
                    : MENU_IDLE_UNFOCUSED_INTERVAL_MS;
        }

        if (unfocusedSinceMs < 0L) {
            return NORMAL_UNFOCUSED_INTERVAL_MS;
        }

        long unfocusedFor = now - unfocusedSinceMs;
        if (unfocusedFor <= BACKGROUND_GRACE_MS) {
            return FAST_UNFOCUSED_INTERVAL_MS;
        }
        if (unfocusedFor <= BACKGROUND_IDLE_MS) {
            return NORMAL_UNFOCUSED_INTERVAL_MS;
        }
        return IDLE_UNFOCUSED_INTERVAL_MS;
    }

    private static boolean isWindowIconified(MinecraftClient client) {
        if (client == null) return false;
        long handle = client.getWindow().getHandle();
        return handle != 0L && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }
}
