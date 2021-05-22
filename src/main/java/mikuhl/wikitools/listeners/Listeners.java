package mikuhl.wikitools.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mikuhl.wikitools.WikiTools;
import mikuhl.wikitools.WikiToolsKeybinds;
import mikuhl.wikitools.entity.EntityRenderClone;
import mikuhl.wikitools.gui.WTGuiScreen;
import mikuhl.wikitools.helper.ClipboardHelper;
import net.minecraft.block.BlockSkull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.input.Keyboard;

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
    public void checkForInventoryButtons(GuiScreenEvent.KeyboardInputEvent.Pre event)
    {
        if (!Keyboard.getEventKeyState())
            return;

        if (Keyboard.isKeyDown(WikiToolsKeybinds.COPY_SKULL_ID.getKeyCode()))
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

                ClipboardHelper.setClipboard(skullID);

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.copiedSkullID") + " " + skullID));
            }
        } else if (Keyboard.isKeyDown(WikiToolsKeybinds.COPY_WIKI_TOOLTIP.getKeyCode()))
        {
            if (event.gui instanceof GuiContainer)
            {
                GuiContainer guiContainer = (GuiContainer) event.gui;
                if (guiContainer.getSlotUnderMouse() == null)
                    return;

                ItemStack is = guiContainer.getSlotUnderMouse().getStack();
                if (is == null)
                    return;
                String ID = "['" + is.getDisplayName().replaceAll("\u00A7.", "") + "']";
                String name = "name = '" + is.getDisplayName().replaceAll("\u00A7.", "") + "'";
                String title = "title = '" + is.getDisplayName().replaceAll("\u00A7", "&") + "'";
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
                        text += lore.getStringTagAt(i).replaceAll("\u00A7", "&")
                                .replaceAll("\\/", "\\\\/").replaceAll("\\,", "\\\\,");
                    }
                }
                text += "'";

                ClipboardHelper.setClipboard(ID + " = {" + name + ", " + title + ", " + text + ",},");

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.copiedTooltip")));
            }
        } else if (Keyboard.isKeyDown(WikiToolsKeybinds.COPY_WIKI_UI.getKeyCode()))
        {
            boolean shift = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
            boolean sprint = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode());

            String close = "\n|close=none";
            String arrow = "\n|arrow=none";
            String goback = "";

            if (event.gui instanceof GuiContainer)
            {
                Minecraft mc = Minecraft.getMinecraft();

                if (mc.thePlayer.openContainer instanceof ContainerChest)
                {
                    ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                    String ui = "{{UI|" + chest.getLowerChestInventory().getName() + (shift ? "|fill=false" : "");

                    for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++)
                    {
                        if (i % 9 == 0 && i != 0)
                            ui += "\n|-";

                        if (!chest.getSlot(i).getHasStack())
                        {
                            if (!shift)
                                ui += "\n|" + ((i / 9) + 1) + ", " + ((i % 9) + 1) + "= , none";
                            continue;
                        }
                        if (chest.getSlot(i).getStack().hasDisplayName())
                            if (chest.getSlot(i).getStack().getDisplayName().equalsIgnoreCase(" "))
                            {
                                if (shift)
                                    ui += "\n|" + ((i / 9) + 1) + ", " + ((i % 9) + 1) + "=Blank, none";
                                continue;
                            } else if (chest.getSlot(i).getStack().getDisplayName().equalsIgnoreCase("\u00A7cClose"))
                            {
                                close = "\n|close=" + ((i / 9) + 1) + ", " + ((i % 9) + 1);
                                continue;
                            } else if (chest.getSlot(i).getStack().getItem().getUnlocalizedName().equalsIgnoreCase("item.arrow")
                                    && chest.getSlot(i).getStack().getDisplayName().contains("Back"))
                            {
                                arrow = "\n|arrow=" + ((i / 9) + 1) + ", " + ((i % 9) + 1);
                                if (chest.getSlot(i).getStack().hasTagCompound() &&
                                        chest.getSlot(i).getStack().getTagCompound().hasKey("display") &&
                                        chest.getSlot(i).getStack().getTagCompound().getCompoundTag("display").hasKey("Lore"))
                                {
                                    goback = "\n|goback=";
                                    NBTTagList lore = chest.getSlot(i).getStack().getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                                    for (int l = 0; l < lore.tagCount(); l++)
                                    {
                                        if (l > 0)
                                            goback += "/";
                                        goback += lore.getStringTagAt(l).replaceAll("\u00A7", "&")
                                                .replaceAll("\\/", "\\\\/").replaceAll("\\,", "\\\\,");
                                    }
                                }
                                continue;
                            }

                        ui += "\n|" + ((i / 9) + 1) + ", " + ((i % 9) + 1) + "=";

                        if (chest.getSlot(i).getStack().getItem() instanceof ItemSkull
                                || (!sprint
                                && chest.getSlot(i).getStack().hasTagCompound()
                                && chest.getSlot(i).getStack().getTagCompound().hasKey("ExtraAttributes")
                                && chest.getSlot(i).getStack().getTagCompound().getCompoundTag("ExtraAttributes").hasKey("id")
                                && !chest.getLowerChestInventory().getName().contains("Collection")))
                        {
                            ui += chest.getSlot(i).getStack().getDisplayName().replaceAll("\u00A7.", "");
                        } else
                            ui += chest.getSlot(i).getStack().getItem().getItemStackDisplayName(chest.getSlot(i).getStack());

                        if (chest.getSlot(i).getStack().stackSize > 1)
                            ui += "; " + chest.getSlot(i).getStack().stackSize;

                        ui += ", none, " + chest.getSlot(i).getStack().getDisplayName().replaceAll("\u00A7", "&")
                                + ",";
                        if (chest.getSlot(i).getStack().hasTagCompound() &&
                                chest.getSlot(i).getStack().getTagCompound().hasKey("display") &&
                                chest.getSlot(i).getStack().getTagCompound().getCompoundTag("display").hasKey("Lore"))
                        {
                            NBTTagList lore = chest.getSlot(i).getStack().getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                            for (int l = 0; l < lore.tagCount(); l++)
                            {
                                if (l > 0)
                                    ui += "/";
                                ui += lore.getStringTagAt(l).replaceAll("\u00A7", "&")
                                        .replaceAll("\\/", "\\\\/").replaceAll("\\,", "\\\\,");
                            }
                        } else
                            ui += "none";
                    }

                    if (!close.equalsIgnoreCase("\n|close=6, 5"))
                        ui += close;
                    if (!arrow.equalsIgnoreCase("\n|arrow=6, 4"))
                        ui += arrow;
                    if (!goback.isEmpty())
                        ui += goback;

                    ui += "\n}}";

                    ClipboardHelper.setClipboard(ui);

                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.copiedUi")));
                }
            }
        }
    }

    /**
     * Called when the Player hovers over an Item
     *
     * @param event Item Tooltip Event
     */
    @SubscribeEvent
    public void checkForTooltips(ItemTooltipEvent event)
    {
        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips)
        {
            ItemStack is = event.itemStack;
            if (is == null ||
                    !is.hasTagCompound() ||
                    !is.getTagCompound().hasKey("ExtraAttributes") ||
                    !is.getTagCompound().getCompoundTag("ExtraAttributes").hasKey("id"))
                return;
            String id = is.getTagCompound().getCompoundTag("ExtraAttributes").getString("id");
            event.toolTip.add("Skyblock ID: " + id);
        }
    }

    /**
     * Keybinds in this function only apply when NOT in a GUI
     *
     * @param event Client Tick Event
     */
    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
            return;

        if (WikiToolsKeybinds.COPY_SKULL_ID.isPressed())
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            MovingObjectPosition objectMouseOver = minecraft.objectMouseOver;
            Entity entity = objectMouseOver.entityHit;

            if (entity != null)
            {
                if (entity instanceof EntityLivingBase)
                {
                    NBTTagCompound nbt = new NBTTagCompound();
                    entity.writeToNBT(nbt);

                    if (!nbt.hasKey("Equipment") ||
                            !nbt.getTagList("Equipment", 10).getCompoundTagAt(4).getCompoundTag("tag").hasKey("SkullOwner"))
                        return;
                    String base64 = nbt.getTagList("Equipment", 10).getCompoundTagAt(4).getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value");
                    JsonElement decoded = new JsonParser().parse(new String(Base64.decodeBase64(base64)));
                    String skullID = decoded.getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString().split("/")[4];

                    ClipboardHelper.setClipboard(skullID);

                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.copiedSkullID") + " " + skullID));
                }
            } else
            {
                BlockPos pos = objectMouseOver.getBlockPos();
                TileEntity tile = minecraft.theWorld.getTileEntity(pos);
                if (tile != null && tile.getBlockType() instanceof BlockSkull)
                {
                    if (!tile.serializeNBT().hasKey("Owner"))
                        return;
                    String base64 = tile.serializeNBT().getCompoundTag("Owner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value");
                    JsonElement decoded = new JsonParser().parse(new String(Base64.decodeBase64(base64)));
                    String skullID = decoded.getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString().split("/")[4];

                    ClipboardHelper.setClipboard(skullID);

                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.copiedSkullID") + " " + skullID));
                }
            }
        }
    }
}
