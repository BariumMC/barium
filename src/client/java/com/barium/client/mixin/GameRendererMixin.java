// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/GameRendererMixin.java ---
package com.barium.client.mixin;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Injeta no final do método onResized.
     * Depois que o Minecraft redimensiona todos os seus framebuffers, nós interceptamos
     * e forçamos o framebuffer do contorno da entidade para metade da resolução,
     * se a opção estiver ativada.
     */
    @Inject(
        method = "onResized(II)V",
        at = @At("RETURN")
    )
    private void barium$forceResizeEntityOutlineFramebuffer(int width, int height, CallbackInfo ci) {
        if (BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES) {
            Framebuffer entityOutlinesFramebuffer = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
            
            if (entityOutlinesFramebuffer != null) {
                // CORREÇÃO: Removemos o terceiro argumento (MinecraftClient.IS_SYSTEM_MAC),
                // pois o método resize agora só aceita a largura e a altura.
                entityOutlinesFramebuffer.resize(width / 2, height / 2);
            }
        }
    }

    @Redirect(
        method = "renderWorld(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/render/Camera;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;Lnet/minecraft/entity/Entity;FZF)V")
    )
    private void barium$disableFog(Camera camera, BackgroundRenderer.FogType fogType, Entity entity, float viewDistance, boolean thickFog, float tickProgress) {
        if (!BariumConfig.C.DISABLE_FOG) {
            BackgroundRenderer.applyFog(camera, fogType, entity, viewDistance, thickFog, tickProgress);
        }
    }
}