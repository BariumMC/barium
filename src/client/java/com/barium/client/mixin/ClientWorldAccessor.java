package com.barium.client.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("blockEntities")
    Map<BlockPos, BlockEntity> getBlockEntityMap();
}