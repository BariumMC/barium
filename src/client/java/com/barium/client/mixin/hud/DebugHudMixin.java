package com.barium.client.mixin.hud;

import com.barium.client.optimization.HudOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    /**
     * Injeta no HEAD do método getDebugInfo para otimizar a geração das linhas do Debug HUD.
     * Se a otimização determinar que não é necessário recalcular, ela cancela a execução
     * original e retorna as linhas cacheadas.
     *
     * @param client O cliente Minecraft.
     * @param cir O CallbackInfo para controlar o retorno e cancelamento.
     */
    @Inject(method = "getDebugInfo", at = @At("HEAD"), cancellable = true)
    private void barium$optimizeDebugInfo(MinecraftClient client, CallbackInfoReturnable<List<String>> cir) {
        // Gera as linhas originais primeiro para que HudOptimizer possa comparar
        // Nota: Esta é uma chamada ao método original do DebugHud.getDebugInfo,
        // mas feita *dentro* do Mixin para que HudOptimizer possa ter acesso às linhas completas.
        // O método "super" aqui é o 'this' em termos de DebugHud.
        List<String> originalLines = ((DebugHud)(Object)this).getDebugInfo(client); // Chama o método original

        // Passa as linhas originais para o otimizador que decidirá se as usa ou retorna as cacheadas
        List<String> optimizedLines = HudOptimizer.getOptimizedDebugInfo(client, originalLines);

        // Se as linhas otimizadas são diferentes das originais (ou seja, foram cacheadas e não recalculadas)
        // então cancelamos a execução original e retornamos as linhas otimizadas.
        if (optimizedLines != originalLines) {
            cir.setReturnValue(optimizedLines);
            cir.cancel();
        }
        // Se optimizedLines == originalLines, a execução original continuará normalmente,
        // pois o Inject no HEAD já obteve as 'originalLines'. Não precisamos cancelar nem setar.
        // O SpongePowered/Mixin lida com a continuação do método automaticamente.
    }
}