package com.barium.client.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker; // <--- MUDAR DE @Accessor PARA @Invoker

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    // Usamos @Invoker para métodos. O nome do método aqui deve corresponder ao método do Minecraft.
    // 'getProfiler()' é o nome real do método na classe MinecraftClient.
    @Invoker("getProfiler") // <--- MUDAR DE @Accessor("field_XXXX") PARA @Invoker("getProfiler")
    Profiler callGetProfiler(); // <--- O nome do seu método no Accessor pode ser qualquer coisa (ex: callGetProfiler, invokeGetProfiler)
}