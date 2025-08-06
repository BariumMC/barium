package com.barium.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Function;

public class OptionPage {
    private final Text title;
    private final Function<Screen, Screen> screenFactory;

    public OptionPage(Text title, Function<Screen, Screen> screenFactory) {
        this.title = title;
        this.screenFactory = screenFactory;
    }

    public Text getTitle() {
        return this.title;
    }

    public Screen createScreen(Screen parent) {
        return this.screenFactory.apply(parent);
    }
}