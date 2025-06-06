package com.barium.client.mixin.resource;

import com.barium.client.optimization.resource.AsyncResourceLoader;
import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    // Adicionado @Final pois no código-fonte do Minecraft o mapa é final.
    @Shadow @Final
    private Map<Identifier, AbstractTexture> textures;

    // CORREÇÃO FINAL: Apontando para o método correto 'registerTexture' com a assinatura completa
    // para resolver a ambiguidade causada pelos três métodos com o mesmo nome.
    @Inject(
        method = "registerTexture(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/texture/AbstractTexture;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$loadTextureAsync(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        if (!BariumConfig.ENABLE_ASYNC_RESOURCE_LOADING) {
            return; // Se desativado, permite que o método original execute normalmente.
        }

        // A lógica do Shadow field requer que o 'this' seja usado para acessar o mapa.
        if (this.textures.containsKey(id)) {
            return;
        }

        // Submete a tarefa de carregamento da textura para o nosso executor.
        // A textura é carregada em uma thread separada.
        AsyncResourceLoader.submit(() -> {
            texture.load(MinecraftClient.getInstance().getResourceManager());
        });

        // Adiciona a textura (ainda não carregada) ao mapa imediatamente.
        // O Minecraft vai usá-la e, quando o carregamento terminar, ela aparecerá.
        this.textures.put(id, texture);

        // Cancela a execução do método original, pois já cuidamos de tudo.
        ci.cancel();
    }
}