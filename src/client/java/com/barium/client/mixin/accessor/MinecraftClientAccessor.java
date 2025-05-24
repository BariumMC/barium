package com.barium.client.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler; // Importar Profiler
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("profiler") // Assume que o campo se chama 'profiler' nos mapeamentos
    Profiler getProfiler_(); // Nome do método com '_' para evitar conflitos, use este método no código
}