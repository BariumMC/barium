package com.barium.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
// Este Mixin agora fica vazio para restaurar a resolução nativa da Janela/GUI.
@Mixin(net.minecraft.client.util.Window.class)
public class WindowMixin {
}