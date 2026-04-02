package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

/**
 * Otimizações adaptativas de tick focadas em reduzir CPU sem quebrar gameplay:
 * - Nunca afeta jogador local;
 * - Só aplica culling mais agressivo para entidades fora da distância de render;
 * - Throttle extra apenas em estados de baixo impacto (background/minimizado).
 */
public final class TickOptimizer {
    private static int particleTickCounter = 0;
    private static int soundTickCounter = 0;

    private TickOptimizer() {
    }

    public static boolean shouldSkipEntityTick(Entity entity, ClientPlayerEntity player, MinecraftClient client) {
        if (!BariumConfig.C.ENABLE_ENTITY_TICK_CULLING) return false;
        if (entity == null || player == null || client == null) return false;
        if (entity.isPlayer() || entity.hasPassengers() || entity.hasVehicle()) return false;

        double distanceSq = entity.squaredDistanceTo(player);
        if (distanceSq <= 40.0 * 40.0) return false;

        boolean offscreenByDirection = isLikelyOffscreenFromPlayer(player, entity);
        boolean beyondRenderDistance = !entity.shouldRender(distanceSq);

        // Em alcance médio, só cull se estiver fora da direção de visão.
        if (distanceSq <= 64.0 * 64.0 && !offscreenByDirection) return false;

        // Em alcance alto, cull por distância ou por estar fora da direção de visão.
        if (!beyondRenderDistance && !offscreenByDirection) return false;

        int divider;
        if (distanceSq > 196.0 * 196.0) {
            divider = 10;
        } else if (distanceSq > 128.0 * 128.0) {
            divider = 8;
        } else if (distanceSq > 96.0 * 96.0) {
            divider = 6;
        } else if (distanceSq > 64.0 * 64.0) {
            divider = 4;
        } else {
            divider = 2;
        }

        if (offscreenByDirection && distanceSq > 48.0 * 48.0) {
            divider = Math.max(divider, 3);
        }

        int fps = client.getCurrentFps();
        if (fps > 0 && fps < 45) {
            divider += 1;
        }

        if (!client.isWindowFocused()) {
            divider += 2;
        }

        return (entity.age % divider) != 0;
    }

    public static boolean shouldSkipRandomBlockDisplayTicks(ClientWorld world, MinecraftClient client) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return false;
        if (world == null || client == null) return false;

        long worldTime = world.getTime();
        if (!client.isWindowFocused()) {
            return (worldTime & 3L) != 0L; // 25% dos ticks em background.
        }

        int fps = client.getCurrentFps();
        if (fps > 0 && fps < 35) {
            return (worldTime % 3L) != 0L; // ~33% quando FPS muito baixo.
        }
        if (fps > 0 && fps < 55) {
            return (worldTime & 1L) != 0L; // 50% quando FPS abaixo da meta.
        }
        return false;
    }

    public static boolean shouldSkipParticleManagerTick(MinecraftClient client) {
        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return false;
        if (client == null) return false;

        particleTickCounter++;
        if (isWindowIconified(client)) {
            return (particleTickCounter & 3) != 0; // 25% minimizado.
        }
        if (!client.isWindowFocused()) {
            return (particleTickCounter & 1) != 0; // 50% unfocused.
        }

        int fps = client.getCurrentFps();
        return fps > 0 && fps < 35 && (particleTickCounter & 1) != 0;
    }

    public static boolean shouldSkipSoundManagerTick(MinecraftClient client) {
        if (client == null) return false;

        soundTickCounter++;
        if (isWindowIconified(client)) {
            return (soundTickCounter & 3) != 0; // mantém áudio vivo com custo menor.
        }
        if (!client.isWindowFocused()) {
            return (soundTickCounter & 1) != 0;
        }
        return false;
    }

    private static boolean isWindowIconified(MinecraftClient client) {
        long handle = client.getWindow().getHandle();
        return handle != 0L && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }

    private static boolean isLikelyOffscreenFromPlayer(ClientPlayerEntity player, Entity entity) {
        double dx = entity.getX() - player.getX();
        double dy = entity.getEyeY() - player.getEyeY();
        double dz = entity.getZ() - player.getZ();
        double lenSq = dx * dx + dy * dy + dz * dz;
        if (lenSq < 1.0e-6) return false;

        double invLen = 1.0 / Math.sqrt(lenSq);
        double dirX = dx * invLen;
        double dirY = dy * invLen;
        double dirZ = dz * invLen;

        var look = player.getRotationVec(1.0F);
        double dot = look.x * dirX + look.y * dirY + look.z * dirZ;
        return dot < 0.15; // fora do cone frontal principal.
    }
}
