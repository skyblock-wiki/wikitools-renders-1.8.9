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
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Listeners {
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
        {
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
        } else if (Keyboard.getEventKey() == WikiToolsKeybinds.COPY_WIKI_TOOLTIP.getKeyCode())
        {
            if (event.gui instanceof GuiContainer)
            {
                GuiContainer guiContainer = (GuiContainer) event.gui;
                if (guiContainer.getSlotUnderMouse() == null)
                    return;

                ItemStack is = guiContainer.getSlotUnderMouse().getStack();
                if (is == null)
                    return;
                String ID = "[" + is.getDisplayName().replaceAll("§.", "") + "]";
                String name = "name='" + is.getDisplayName().replaceAll("§.", "") + "'";
                String title = "title = '" + is.getDisplayName().replaceAll("§", "&") + "'";
                String text = "text = '";
                if (is.hasTagCompound() &&
                        is.getTagCompound().hasKey("display") &&
                        is.getTagCompound().getCompoundTag("display").hasKey("Lore"))
                {
                    NBTTagList lore = is.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                    for (int i = 0; i < lore.tagCount(); i++)
                    {
                        if (i > 0)
                            text += "/";
                        text += lore.getStringTagAt(i).replaceAll("§", "&");
                    }
                }
                text += "'";
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(ID + " = {" + name + ", " + title + ", " + text + ",},"), null);

            }
        }
    }

    @SubscribeEvent
    public void checkForTooltips(ItemTooltipEvent event)
    {
        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips)
        {
            ItemStack is = event.itemStack;
            if (is == null ||
                    !is.hasTagCompound() ||
                    !is.getTagCompound().hasKey("ExtraAttributes"))
                return;
            String id = is.getTagCompound().getCompoundTag("ExtraAttributes").getString("id");
            event.toolTip.add("Skyblock ID: " + id);
        }
    }
}
