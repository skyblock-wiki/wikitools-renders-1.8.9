package mikuhl.wikitools.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mikuhl.wikitools.WikiTools;
import mikuhl.wikitools.WikiToolsKeybinds;
import mikuhl.wikitools.entity.EntityRenderClone;
import mikuhl.wikitools.gui.WTGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class RenderListener {
    public boolean openUI = false;

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e)
    {
        if (openUI)
        {
            if (WikiTools.getInstance().getEntity() == null)
                WikiTools.getInstance().setEntity(new EntityRenderClone(Minecraft.getMinecraft().thePlayer, false));
            Minecraft.getMinecraft().displayGuiScreen(new WTGuiScreen());
        }
        openUI = false;
    }

    @SubscribeEvent
    public void checkForInventoryButtons(GuiScreenEvent.KeyboardInputEvent event)
    {
        if (Keyboard.getEventKey() == WikiToolsKeybinds.COPY_SKULL_ID.getKeyCode())
            if (event.gui instanceof GuiContainer)
            {
                GuiContainer guiContainer = (GuiContainer) event.gui;
                if (guiContainer.getSlotUnderMouse() == null)
                    return;

                ItemStack is = guiContainer.getSlotUnderMouse().getStack();
                if (is == null ||
                        !(is.getItem() instanceof ItemSkull) ||
                        !is.hasTagCompound() ||
                        !is.getTagCompound().hasKey("SkullOwner"))
                    return;
                String base64 = is.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value");
                JsonElement decoded = new JsonParser().parse(new String(Base64.decodeBase64(base64)));
                String skullID = decoded.getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString().split("/")[4];
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(skullID), null);
            }
    }
}
