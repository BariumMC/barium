package com.barium.client.mixin;

import com.barium.client.optimization.HotbarRenderOptimizer;
import com.barium.config.BariumConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"), require = 0)
    private void barium$beginHudFrame(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HotbarRenderOptimizer.beginHudFrame();
    }

    /**
     * Evita custo de caminho de render de item da hotbar para slots vazios.
     * Em cenas comuns, vários slots ficam vazios e esse atalho reduz chamadas
     * internas de DrawContext/ItemModelManager que seriam no-op.
     */
    @Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void barium$skipEmptyHotbarItem(
            DrawContext context,
            int x,
            int y,
            RenderTickCounter tickCounter,
            PlayerEntity player,
            ItemStack stack,
            int seed,
            CallbackInfo ci
    ) {
        if (!BariumConfig.C.ENABLE_GUI_OPTIMIZATION) return;
        if (stack == null || stack.isEmpty()) {
            ci.cancel();
        }
    }
}
