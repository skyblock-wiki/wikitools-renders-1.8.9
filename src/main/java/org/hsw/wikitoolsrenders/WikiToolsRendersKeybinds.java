package org.hsw.wikitoolsrenders;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class WikiToolsRendersKeybinds {

    private static String category = "wikitoolsrenders.keybind.category";

    public static KeyBinding COPY_ENTITY = new KeyBinding(
            "wikitoolsrenders.keybind.copyEntity",
            Keyboard.KEY_M,
            category
    );

    public static KeyBinding HUD = new KeyBinding(
            "wikitoolsrenders.keybind.hud",
            Keyboard.KEY_K,
            category
    );

    public static void init() {
        ClientRegistry.registerKeyBinding(COPY_ENTITY);
        ClientRegistry.registerKeyBinding(HUD);
    }

}
