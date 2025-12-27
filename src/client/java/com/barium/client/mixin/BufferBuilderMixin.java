package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin {

    @Shadow private boolean building;

    /**
     * Otimização de Micro-operação: 
     * O método vertex() verifica se 'building' é true a cada chamada.
     * Em cenários de alto FPS, isso acontece milhões de vezes.
     * 
     * Nota: Não podemos remover a verificação facilmente com Mixin simples sem sobrescrever,
     * mas podemos otimizar a alocação forçando o buffer a crescer de forma mais inteligente.
     */
    
    // Otimização "Ensure Capacity" - Reduz redimensionamento de array durante drawIndexed
    @Inject(method = "begin", at = @At("TAIL"))
    private void barium$smartCapacity(VertexFormat.DrawMode drawMode, VertexFormat format, CallbackInfo ci) {
         // Não implementado para evitar crashes de memória, mas o mixin está pronto para expansão.
    }
}