package com.barium.client.optimization;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Reduz custo de aplicação de pacotes de atualização em rajadas para entidades
 * distantes e fora da visão. Mantém consistência para entidades próximas/visíveis.
 */
public final class NetworkPacketOptimizer {
    private static final double FAR_UPDATE_DISTANCE_SQ = 96.0 * 96.0;
    private static final Map<Integer, Long> LAST_TRACKER_APPLY_TICK = new HashMap<>();
    private static final Map<Integer, Long> LAST_ATTRIBUTE_APPLY_TICK = new HashMap<>();

    private static Method idAccessor;
    private static boolean idAccessorResolved;

    private NetworkPacketOptimizer() {
    }

    public static Integer extractEntityId(Object packet) {
        if (packet == null) return null;
        if (!idAccessorResolved) {
            idAccessor = resolveIdAccessor(packet.getClass());
            idAccessorResolved = true;
        }
        if (idAccessor == null) return null;
        try {
            Object result = idAccessor.invoke(packet);
            return result instanceof Integer ? (Integer) result : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean shouldThrottleTrackerUpdate(MinecraftClient client, Entity entity) {
        if (client == null || entity == null || client.player == null) return false;
        if (entity.isPlayer()) return false;
        if (entity.squaredDistanceTo(client.player) <= FAR_UPDATE_DISTANCE_SQ) return false;
        if (entity.shouldRender(entity.squaredDistanceTo(client.player))) return false;

        long worldTime = client.world != null ? client.world.getTime() : 0L;
        int entityId = entity.getId();
        long last = LAST_TRACKER_APPLY_TICK.getOrDefault(entityId, Long.MIN_VALUE);
        if (worldTime - last < 2L) {
            return true; // no máximo 1 update a cada 2 ticks para entidades distantes.
        }
        LAST_TRACKER_APPLY_TICK.put(entityId, worldTime);
        return false;
    }

    public static boolean shouldThrottleAttributeUpdate(MinecraftClient client, Entity entity) {
        if (client == null || entity == null || client.player == null) return false;
        if (entity.squaredDistanceTo(client.player) <= FAR_UPDATE_DISTANCE_SQ) return false;

        long worldTime = client.world != null ? client.world.getTime() : 0L;
        int entityId = entity.getId();
        long last = LAST_ATTRIBUTE_APPLY_TICK.getOrDefault(entityId, Long.MIN_VALUE);
        if (worldTime - last < 4L) {
            return true;
        }
        LAST_ATTRIBUTE_APPLY_TICK.put(entityId, worldTime);
        return false;
    }

    private static Method resolveIdAccessor(Class<?> packetClass) {
        String[] candidates = {"id", "getId", "entityId", "getEntityId"};
        for (String name : candidates) {
            try {
                Method m = packetClass.getMethod(name);
                if (m.getReturnType() == int.class || m.getReturnType() == Integer.class) {
                    m.setAccessible(true);
                    return m;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}
