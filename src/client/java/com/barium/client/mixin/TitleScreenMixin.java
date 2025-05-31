package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.MenuHudOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para TitleScreen para otimizar a renderização do menu principal.
 * - Controla o FPS do panorama de fundo.
 * - Cacheia elementos estáticos da UI como texto de versão e copyright.
 *
 * Target Class: net.minecraft.client.gui.screen.TitleScreen
 * Mappings: Yarn 1.21.5+build.1
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    // Construtor obrigatório para Mixins em classes com construtores não-padrão
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    /**
     * Injeta no início do método render() da TitleScreen.
     * Usado para garantir que o cache da UI estática está atualizado antes de qualquer desenho.
     *
     * @param context O contexto de desenho.
     * @param mouseX Posição X do mouse.
     * @param mouseY Posição Y do mouse.
     * @param delta Tempo delta do frame.
     * @param ci CallbackInfo.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void barium$onRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Assegura que o cache de UI estática está atualizado se necessário
        if (BariumConfig.ENABLE_MENU_OPTIMIZATION && BariumConfig.CACHE_MENU_STATIC_UI) {
            // CORRIGIDO: Não passar 'context' para updateStaticUiCache
            MenuHudOptimizer.updateStaticUiCache(this.width, this.height);
        }
    }

    /**
     * Injeta no método render da TitleScreen para pular a renderização do panorama
     * se o otimizador indicar que um novo frame não é necessário.
     *
     * Target: Injeção ANTES da chamada a `net.minecraft.client.gui.screen.TitleScreen$BackgroundRenderer.render(Lnet/minecraft/client/gui/DrawContext;F)V`
     * A classe interna BackgroundRenderer é responsável pelo panorama de fundo.
     *
     * @param context O DrawContext.
     * @param mouseX X do mouse.
     * @param mouseY Y do mouse.
     * @param delta Delta time.
     * @param ci CallbackInfo.
     */
    @Inject(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen$BackgroundRenderer;render(Lnet/minecraft/client/gui/DrawContext;F)V", shift = At.Shift.BEFORE),
        cancellable = true
    )
    private void barium$skipPanoramaRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MenuHudOptimizer.shouldRenderPanoramaFrame()) {
            ci.cancel();
        }
    }

    /**
     * Injeta no final do método render() da TitleScreen para desenhar o cache de UI estática.
     *
     * @param context O contexto de desenho.
     * @param mouseX Posição X do mouse.
     * @param mouseY Posição Y do mouse.
     * @param delta Tempo delta do frame.
     * @param ci CallbackInfo.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void barium$onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (MenuHudOptimizer.isStaticUiCacheValid()) {
            MenuHudOptimizer.drawCachedStaticUi(context);
        }
    }

    /**
     * Injeta no método init() da TitleScreen (chamado na inicialização da tela ou resize)
     * para limpar o cache da UI estática e garantir que ele seja recriado na próxima renderização.
     *
     * @param ci CallbackInfo.
     */
    @Inject(method = "init", at = @At("HEAD"))
    private void barium$onInit(CallbackInfo ci) {
        MenuHudOptimizer.clearCache();
    }

    /**
     * Injeta no método close() da TitleScreen (chamado ao sair da tela)
     * para limpar o cache e liberar os recursos do framebuffer.
     *
     * @param ci CallbackInfo.
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void barium$onClose(CallbackInfo ci) {
        MenuHudOptimizer.clearCache();
    }
}