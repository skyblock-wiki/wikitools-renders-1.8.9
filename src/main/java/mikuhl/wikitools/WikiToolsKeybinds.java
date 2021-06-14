package mikuhl.wikitools;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class WikiToolsKeybinds {

    private static String     category          = "mikuhl.wikitool.category";
    public static  KeyBinding COPY_ENTITY       = new KeyBinding(
            "wikitools.keybind.copyEntity",
            Keyboard.KEY_M,
            category
    );
    public static  KeyBinding SELF_MODIFIER     = new KeyBinding(
            "mikuhl.wikitool.self.modifier.description",
            Keyboard.KEY_RMENU,
            category
    );
    public static  KeyBinding COPY_NBT          = new KeyBinding(
            "mikuhl.wikitool.copy.nbt.description",
            Keyboard.KEY_N,
            category
    );
    public static  KeyBinding HUD               = new KeyBinding(
            "wikitools.keybind.hud",
            Keyboard.KEY_K,
            category
    );
    public static  KeyBinding COPY_SKULL_ID     = new KeyBinding(
            "wikitools.keybind.skullID",
            Keyboard.KEY_Z,
            category
    );
    public static  KeyBinding COPY_WIKI_TOOLTIP = new KeyBinding(
            "wikitools.keybind.tooltip",
            Keyboard.KEY_X,
            category
    );
    public static  KeyBinding COPY_WIKI_UI      = new KeyBinding(
            "wikitools.keybind.ui",
            Keyboard.KEY_C,
            category
    );

    public static void init()
    {
        ClientRegistry.registerKeyBinding(COPY_ENTITY);
        ClientRegistry.registerKeyBinding(COPY_NBT);
        ClientRegistry.registerKeyBinding(HUD);
        ClientRegistry.registerKeyBinding(COPY_SKULL_ID);
        ClientRegistry.registerKeyBinding(COPY_WIKI_TOOLTIP);
        ClientRegistry.registerKeyBinding(COPY_WIKI_UI);
    }
}
