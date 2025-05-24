package com.barium.client.mixin.chunk_building;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChunkBuilder.class)
public abstract class ChunkBuilderMixin {

    // Modifica o número de threads usadas pelo ChunkBuilder
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 2)) // Altere o valor intValue para o padrão da versão
    private int barium$modifyChunkBuilderThreadCount(int original) {
        int configuredThreads = BariumConfig.get().chunkBuilding.chunkBuilderThreads;
        if (configuredThreads > 0) {
            BariumMod.LOGGER.debug("Configurando ChunkBuilder para usar {} threads.", configuredThreads);
            return configuredThreads;
        }
        return original; // Retorna o valor original se a configuração for inválida
    }
}