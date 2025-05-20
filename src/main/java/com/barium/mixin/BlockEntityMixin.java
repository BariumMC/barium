package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        
        // Se for um HopperBlockEntity, aplica a lógica de otimização
        if (blockEntity instanceof HopperBlockEntity) {
            HopperBlockEntity hopper = (HopperBlockEntity) blockEntity;
            
            // Se a otimização decidir que o tick não é necessário, cancelamos
            if (!BlockTickOptimizer.shouldTickHopper(hopper, blockEntity.getPos())) {
                ci.cancel(); 
            } else {
                // Se o tick deve ocorrer, chamamos o método original via invoker.
                // Obtemos o mundo, a posição e o estado atual do bloco.
                World world = hopper.getWorld();
                BlockPos pos = hopper.getPos();
                BlockState state = hopper.getCachedState();
                
                // Chama o tick original
                HopperBlockEntityInvoker.invokeTick(world, pos, state, hopper);
                ci.cancel(); // Previne execução duplicada do tick original
            }
        }
    }
    
    // Declaração do invoker para chamar o método estático 'tick' de HopperBlockEntity
    @Mixin(HopperBlockEntity.class)
    public interface HopperBlockEntityInvoker {
        
        @Invoker("tick")
        static void invokeTick(World world, BlockPos pos, BlockState state, HopperBlockEntity hopper) {
            throw new AssertionError();
        }
    }
}
