package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityOutlineOptimizer {

    private static final AtomicBoolean hasGlowingEntities = new AtomicBoolean(false);
    private static int lastCheckTick = -1;

    /**
     * Reseta o estado para o novo frame.
     */
    public static void reset() {
        hasGlowingEntities.set(false);
    }

    /**
     * Marca que detectamos uma entidade brilhando neste frame.
     */
    public static void notifyGlowingEntity() {
        hasGlowingEntities.set(true);
    }

    /**
     * Lógica estilo Sodium: Evita processamento inútil.
     * Verifica se realmente precisamos rodar o pipeline de framebuffer.
     */
    public static boolean shouldProcessOutlines() {
        // 1. Se desativado pelo usuário, corta.
        if (BariumConfig.C.DISABLE_ENTITY_OUTLINES) return false;

        // 2. Otimização de Resolução e Contexto
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return false;

        // 3. Se nenhuma entidade brilhante foi renderizada neste frame,
        // não há motivo para rodar shaders de blur em um buffer vazio.
        return hasGlowingEntities.get();
    }

    /**
     * Retorna o divisor de resolução.
     * O Sodium usa técnicas similares de downsampling para efeitos pós-processamento.
     * 1 = Nativo (Lento)
     * 2 = Metade (Rápido e visualmente idêntico para blur)
     */
    public static int getResolutionDivisor() {
        return BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES ? 2 : 1;
    }
}