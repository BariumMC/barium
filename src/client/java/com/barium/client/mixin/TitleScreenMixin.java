// barium-1.21.5-devs/src/client/java/com/barium/client/mixin/TitleScreenMixin.java
package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject; // Usar Inject para o panorama
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo; // Importar CallbackInfo

/**
 * Mixin específico para TitleScreen para otimizações de GUI client-side.
 * Pode adicionar otimizações mais direcionadas para a tela de título (menu principal).
 * Foco em reduzir o custo de renderização do panorama de fundo e do texto de splash.
 *
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    /**
     * Injeta ANTES da chamada para renderizar o panorama de fundo.
     * Se a otimização estiver habilitada e a opção de desativar o panorama for true,
     * o método original de renderização do panorama é cancelado (pulado).
     * Opcionalmente, pode desenhar um fundo estático no lugar.
     *
     * Target Method: TitleScreen.render(Lnet/minecraft/client/gui/DrawContext;IIF)V
     * Target INVOKE: Lnet/minecraft/client/gui/screen/TitleScreen;renderPanoramaBackground(Lnet/minecraft/client/gui/DrawContext;F)V
     *
     * @param context O contexto de desenho.
     * @param mouseX Posição X do mouse.
     * @param mouseY Posição Y do mouse.
     * @param delta O delta (tempo parcial do tick, que é usado como alpha para o panorama).
     * @param ci CallbackInfo para cancelar o método original.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;renderPanoramaBackground(Lnet/minecraft/client/gui/DrawContext;F)V"),
        cancellable = true // Permite que o método original seja cancelado
    )
    private void barium$onRenderPanoramaBackgroundInvoke(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BariumConfig.ENABLE_TITLE_SCREEN_OPTIMIZATION && BariumConfig.DISABLE_TITLE_SCREEN_PANORAMA) {
            // Otimização: Não renderiza o panorama animado.
            // Opcionalmente, preenche a tela com uma cor sólida para evitar um fundo transparente/vazio.
            if (BariumConfig.DRAW_STATIC_BACKGROUND_IF_PANORAMA_DISABLED) {
                // Desenha um fundo preto sólido. Ajuste a cor conforme necessário (ARGB).
                context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0xFF000000);
            }
            BariumMod.LOGGER.debug("TitleScreenMixin: Panorama background skipped.");
            ci.cancel(); // Cancela a chamada ORIGINAL a renderPanoramaBackground
        }
        // Se as otimizações não estiverem ativadas, o método original (renderPanoramaBackground)
        // será chamado normalmente APÓS esta injeção.
    }

    /**
     * Redireciona a chamada para renderizar o texto de splash.
     * Se a otimização estiver habilitada e a opção de desativar o texto de splash for true,
     * o método original de renderização do texto é pulado.
     *
     * Target Method: TitleScreen.render(Lnet/minecraft/client/gui/DrawContext;IIF)V
     * Target INVOKE: Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V
     *
     * Nota: O ordinal pode precisar de ajuste se houver múltiplas chamadas idênticas
     * a drawCenteredTextWithShadow no método render do TitleScreen.
     * O texto de splash é geralmente a primeira ou uma das primeiras chamadas para String.
     * No 1.21.5, parece ser o primeiro uso de drawCenteredTextWithShadow com uma String literal/campo.
     *
     * @param context A instância do DrawContext.
     * @param textRenderer O renderizador de texto.
     * @param text O texto a ser renderizado (o texto de splash).
     * @param x Posição X centralizada.
     * @param y Posição Y.
     * @param color A cor do texto.
     */
    @Redirect(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V", ordinal = 0)
    )
    private void barium$redirectSplashTextRender(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color) {
        if (BariumConfig.ENABLE_TITLE_SCREEN_OPTIMIZATION && BariumConfig.DISABLE_TITLE_SCREEN_SPLASH_TEXT) {
            // Otimização: Não renderiza o texto de splash.
            BariumMod.LOGGER.debug("TitleScreenMixin: Splash text skipped.");
        } else {
            // Se a otimização estiver desativada ou a opção não for true, chama o método original.
            context.drawCenteredTextWithShadow(textRenderer, text, x, y, color);
        }
    }
}