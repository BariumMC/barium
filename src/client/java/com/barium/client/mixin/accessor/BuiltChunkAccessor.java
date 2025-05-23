package com.barium.client.mixin.accessor;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkBuilder.BuiltChunk.class)
public interface BuiltChunkAccessor {
    @Accessor("origin")
    BlockPos getOriginPos();
}