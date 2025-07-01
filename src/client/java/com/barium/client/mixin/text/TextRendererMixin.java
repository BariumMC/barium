package com.barium.client.mixin.text;

import com.barium.client.BariumClientMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin para otimização de renderização de texto
 * - Implementa cache de textos renderizados
 * - Throttle de renderizações de tooltip
 */
@Mixin(TextRenderer.class)
public class TextRendererMixin {
    
    // Cache de textos renderizados (chave: texto + cor + transparência)
    private static final Map<String, Long> textRenderTimeMap = new HashMap<>();
    private static final Map<String, Integer> textWidthCache = new HashMap<>();
    
    /**
     * Injeta no método de cálculo de largura para implementar cache
     */
    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void onGetWidth(String text, CallbackInfoReturnable<Integer> cir) {
        // Acesso à configuração
        if (!BariumClientMod.getConfig().enableTextOptimizations || 
            !BariumClientMod.getConfig().enableTextCaching) {
            return;
        }
        
        // Verificar se o texto já está no cache
        if (textWidthCache.containsKey(text)) {
            cir.setReturnValue(textWidthCache.get(text));
            return;
        }
        
        // O texto não está no cache, será calculado normalmente
        // O resultado será armazenado no cache pelo método onGetWidthReturn
    }
    
    /**
     * Injeta no retorno do método de cálculo de largura para atualizar o cache
     */
    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("RETURN"))
    private void onGetWidthReturn(String text, CallbackInfoReturnable<Integer> cir) {
        // Acesso à configuração
        if (!BariumClientMod.getConfig().enableTextOptimizations || 
            !BariumClientMod.getConfig().enableTextCaching) {
            return;
        }
        
        // Armazenar o resultado no cache
        textWidthCache.put(text, cir.getReturnValue());
        
        // Limitar o tamanho do cache (evitar vazamento de memória)
        if (textWidthCache.size() > 1000) {
            // TODO: Implementar limpeza de cache baseada em LRU ou tempo
        }
    }
    
    /**
     * Injeta no método de renderização de texto para implementar throttling
     */
    @Inject(method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I", 
            at = @At("HEAD"), cancellable = true)
    private void onDrawText(Text text, float x, float y, int color, boolean shadow, 
                          org.joml.Matrix4f matrix, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, 
                          boolean seeThrough, int backgroundColor, int light, 
                          CallbackInfoReturnable<Integer> cir) {
        // Acesso à configuração
        if (!BariumClientMod.getConfig().enableTextOptimizations || 
            !BariumClientMod.getConfig().enableTooltipThrottling) {
            return;
        }
        
        // Criar uma chave única para este texto
        String key = text.getString() + "_" + color + "_" + x + "_" + y;
        
        // Verificar se o texto foi renderizado recentemente
        long currentTime = System.currentTimeMillis();
        if (textRenderTimeMap.containsKey(key)) {
            long lastRenderTime = textRenderTimeMap.get(key);
            if (currentTime - lastRenderTime < 100) { // 100ms throttle para tooltips
                // Texto renderizado recentemente, usar valor anterior
                // TODO: Implementar retorno do valor anterior
                return;
            }
        }
        
        // Atualizar o tempo de renderização
        textRenderTimeMap.put(key, currentTime);
        
        // Limitar o tamanho do cache (evitar vazamento de memória)
        if (textRenderTimeMap.size() > 1000) {
            // TODO: Implementar limpeza de cache baseada em LRU ou tempo
        }
    }
}
