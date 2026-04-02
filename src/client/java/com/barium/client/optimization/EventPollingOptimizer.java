package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

/**
 * Agendador adaptativo de pollEvents inspirado em técnicas usadas em mods
 * de performance: reduz trabalho quando o jogo está em background sem
 * sacrificar responsividade ao trocar foco.
 */
public final class EventPollingOptimizer {
    private static final long FAST_UNFOCUSED_INTERVAL_MS = 4L;
    private static final long NORMAL_UNFOCUSED_INTERVAL_MS = 8L;
    private static final long IDLE_UNFOCUSED_INTERVAL_MS = 16L;
    private static final long BACKGROUND_GRACE_MS = 2_000L;
    private static final long BACKGROUND_IDLE_MS = 10_000L;

    private static long lastPollMs = 0L;
    private static long unfocusedSinceMs = -1L;
    private static boolean wasFocused = true;

    private EventPollingOptimizer() {
    }

    public static boolean shouldSkipPollEvents(MinecraftClient client) {
        if (!BariumConfig.C.ENABLE_BACKGROUND_EVENT_THROTTLING) return false;
        if (client == null) return false;

        boolean focused = client.isWindowFocused();
        long now = Util.getMeasuringTimeMs();

        if (focused) {
            wasFocused = true;
            unfocusedSinceMs = -1L;
            return false;
        }

        if (wasFocused) {
            wasFocused = false;
            unfocusedSinceMs = now;
        }

        long interval = computeTargetInterval(now);
        return now - lastPollMs < interval;
    }

    public static void markPollExecuted() {
        lastPollMs = Util.getMeasuringTimeMs();
    }

    private static long computeTargetInterval(long now) {
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
}
