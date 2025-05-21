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
     * Injeta no RETORNO do método getDebugInfo() para interceptar seu resultado.
     * Isso permite que o método original execute totalmente e gere a lista de strings,
     * e então nosso otimizador decide se usará essa lista ou uma versão cacheada.
     *
     * @param cir O CallbackInfo para controlar o retorno. O resultado do método original
     *            está em cir.getReturnValue().
     */
    @Inject(method = "getDebugInfo", at = @At("RETURN"), cancellable = true)
    private void barium$optimizeDebugInfo(CallbackInfoReturnable<List<String>> cir) {
        // O método original já foi executado, e seu resultado está em cir.getReturnValue()
        List<String> originalLines = cir.getReturnValue();

        // Obtém a instância do MinecraftClient de forma segura
        MinecraftClient client = MinecraftClient.getInstance();

        // Passa as linhas originais para o otimizador que decidirá se as usa ou retorna as cacheadas
        List<String> optimizedLines = HudOptimizer.getOptimizedDebugInfo(client, originalLines);

        // Se as linhas otimizadas são diferentes das originais (ou seja, foram cacheadas e não recalculadas)
        // então definimos o valor de retorno do Mixin para as linhas otimizadas.
        if (optimizedLines != originalLines) {
            cir.setReturnValue(optimizedLines);
        }
        // Não é necessário chamar ci.cancel() no 'RETURN', apenas definir o setReturnValue é suficiente.
    }
}