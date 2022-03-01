package mikuhl.wikitools.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mikuhl.wikitools.WikiTools;
import mikuhl.wikitools.WikiToolsKeybinds;
import mikuhl.wikitools.entity.RenderPlayerOverride;
import mikuhl.wikitools.gui.WTGuiScreen;
import mikuhl.wikitools.helper.ClipboardHelper;
import net.minecraft.block.BlockSkull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.event.ClickEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.input.Keyboard;

public class Listeners {
    public boolean openUI = false;
    // colors used on wiki
    private final String[] glassPaneColors = {
        "White",
        "Orange",
        "Magenta",
        "Light Blue",
        "Yellow",
        "Lime",
        "Pink",
        "Gray",
        "Light Gray",
        "Cyan",
        "Purple",
        "Blue",
        "Brown",
        "Green",
        "Red",
        "Black"
    };

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e)
    {
        if (openUI)
            Minecraft.getMinecraft().displayGuiScreen(new WTGuiScreen());
        openUI = false;
    }

    @SubscribeEvent
    public void copySkullIDHandler(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Keyboard.getEventKeyState() || !(WikiToolsKeybinds.COPY_SKULL_ID.getKeyCode() >= 0 && Keyboard.isKeyDown(WikiToolsKeybinds.COPY_SKULL_ID.getKeyCode())))
            return;

        if (event.gui instanceof GuiContainer) {
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
    }

    @SubscribeEvent
    public void copyWikiTooltipHandler(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Keyboard.getEventKeyState() || !(WikiToolsKeybinds.COPY_WIKI_TOOLTIP.getKeyCode() >= 0 && Keyboard.isKeyDown(WikiToolsKeybinds.COPY_WIKI_TOOLTIP.getKeyCode())))
            return;

        if (event.gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) event.gui;
            if (guiContainer.getSlotUnderMouse() == null)
                return;

            ItemStack is = guiContainer.getSlotUnderMouse().getStack();
            if (is == null)
                return;
            String ID = "['" + sanitiseAll(is.getDisplayName(), true, false) + "']";
            String name = "name = '" + sanitiseAll(is.getDisplayName(), true, false) + "'";
            String title = "title = '" + sanitiseAll(is.getDisplayName(), false, false) + "'";
            String text = "text = '";
            if (is.hasTagCompound() &&
                    is.getTagCompound().hasKey("display") &&
                    is.getTagCompound().getCompoundTag("display").hasKey("Lore")) {
                NBTTagList lore = is.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                for (int i = 0; i < lore.tagCount(); i++) {
                    if (i > 0)
                        text += "/";
                    text += sanitiseAll(lore.getStringTagAt(i), false, false);
                }
            }
            text += "'";

            ClipboardHelper.setClipboard(ID + " = {" + name + ", " + title + ", " + text + ", },");

            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.copiedTooltip")));
        }
    }

    @SubscribeEvent
    public void copyWikiUIHandler(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Keyboard.getEventKeyState() || !(WikiToolsKeybinds.COPY_WIKI_UI.getKeyCode() >= 0 && Keyboard.isKeyDown(WikiToolsKeybinds.COPY_WIKI_UI.getKeyCode())))
            return;

        // Used for "No-Fill" Mode
        boolean shift = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
        // Used for "use Minecraft item name if not skull" Mode
        boolean sprint = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode());

        if (event.gui instanceof GuiContainer) {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.thePlayer.openContainer instanceof ContainerChest) {
                ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                String close = "\n|close=none";
                String arrow = "\n|arrow=none";
                String goback = "";
                String ui = "{{UI|" + sanitiseColors(chest.getLowerChestInventory().getName(), true)
                        + (shift ? "|fill=false" : "");

                for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
                    if (i % 9 == 0 && i != 0)
                        ui += "\n|-";

                    // |Row, Column= with top left being 1, 1
                    // Note: a future (planned) implementation of this function will require array[cellX][cellY] = line
                    int cellX = (i / 9) + 1, cellY = (i % 9) + 1;
                    String cellpos = cellX + ", " + cellY;
                    String line = "";

                    // If the current slot is empty
                    if (!chest.getSlot(i).getHasStack()) {
                        if (!shift)
                            line += "\n|" + cellpos + "= , none";
                        ui += line; continue;
                    }

                    // Extract to variables
                    ItemStack stack = chest.getSlot(i).getStack();

                    // Handle Custom UI Items
                    if (stack.hasDisplayName())
                        if (stack.getUnlocalizedName().contains("tile.thinStainedGlass")
                            && stack.getDisplayName().equalsIgnoreCase(" ")) {
                            // handle border glass panes
                            String color = glassPaneColors[stack.getItemDamage()];
                            if (!color.equals("Black"))
                                line += "\n|" + cellpos + "=Blank (" + color + "), none";
                            else if (shift)
                                line += "\n|" + cellpos + "=Blank, none";
                            ui += line; continue;
                        } else if (stack.getDisplayName().equalsIgnoreCase("\u00A7cClose")) {
                            close = "\n|close=" + cellpos;
                            ui += line; continue;
                        } else if (stack.getUnlocalizedName().equalsIgnoreCase("item.arrow")
                                && stack.getDisplayName().contains("Back")) {
                            arrow = "\n|arrow=" + cellpos;
                            if (stack.hasTagCompound() &&
                                    stack.getTagCompound().hasKey("display") &&
                                    stack.getTagCompound().getCompoundTag("display").hasKey("Lore")) {
                                goback = "\n|goback=";
                                NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                                for (int l = 0; l < lore.tagCount(); l++) {
                                    if (l > 0)
                                        goback += "/";
                                    goback += sanitiseAll(lore.getStringTagAt(l), false, true);
                                }
                            }
                            ui += line; continue;
                        }

                    // Handle Other UI Items
                    line += "\n|" + cellpos + "=";

                    // parse item name
                    if (stack.getItem() instanceof ItemSkull
                            || (!sprint
                            && stack.hasTagCompound()
                            && stack.getTagCompound().hasKey("ExtraAttributes")
                            && stack.getTagCompound().getCompoundTag("ExtraAttributes").hasKey("id")
                            /* && !chest.getLowerChestInventory().getName().contains("Collection") */)) {
                        // normally, use displayed name
                        line += sanitiseCommas(sanitiseColors(stack.getDisplayName(), true));
                    } else {
                        // if held CTRL and item is not a skull, use Minecraft item name
                        boolean ench = false;
                        if (stack.hasTagCompound()
                            && stack.getTagCompound().hasKey("tag")
                            && stack.getTagCompound().getCompoundTag("tag").hasKey("ench"))
                            ench = true;
                        line += (ench ? "Enchanted " : "") + stack.getItem().getItemStackDisplayName(stack);
                    }

                    // parse stack size
                    if (stack.stackSize > 1)
                        line += "; " + stack.stackSize;

                    // parse lore title
                    line += ", none, " + sanitiseAll(stack.getDisplayName(), false, true) + ", ";

                    // parse lore text
                    if (stack.hasTagCompound() &&
                            stack.getTagCompound().hasKey("display") &&
                            stack.getTagCompound().getCompoundTag("display").hasKey("Lore")) {
                        NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                        for (int l = 0; l < lore.tagCount(); l++) {
                            if (l > 0)
                                line += "/";
                            line += sanitiseAll(lore.getStringTagAt(l), false, true);
                        }
                    } else
                        line += "none";

                    // add line to ui string
                    ui += line;
                }

                if (!close.equalsIgnoreCase("\n|close=6, 5"))
                    ui += close;
                if (!arrow.equalsIgnoreCase("\n|arrow=6, 4"))
                    ui += arrow;
                if (!goback.isEmpty())
                    ui += goback;

                if (chest.getLowerChestInventory().getSizeInventory() / 9 != 6)
                    ui += "\n|rows=" + chest.getLowerChestInventory().getSizeInventory() / 9;

                ui += "\n}}";

                ClipboardHelper.setClipboard(ui);

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(
                        I18n.format("wikitools.message.copiedUI")
                        + (shift ? " " + I18n.format("wikitools.message.UInofill") : "")
                        + (sprint ? " " + I18n.format("wikitools.message.UImcitem") : "")
                ));
            }
        }
    }

    @SubscribeEvent
    public void copyEntityHandler(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Keyboard.getEventKeyState() || !(WikiToolsKeybinds.COPY_ENTITY.getKeyCode() >= 0 && Keyboard.isKeyDown(WikiToolsKeybinds.COPY_ENTITY.getKeyCode())))
            return;

        if (event.gui instanceof GuiContainer)
        {
            boolean shift = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());

            GuiContainer guiContainer = (GuiContainer) event.gui;
            if (guiContainer.getSlotUnderMouse() == null)
                return;

            ItemStack is = guiContainer.getSlotUnderMouse().getStack();
            if (is == null)
                return;

            if (shift && (is.getItem() instanceof ItemBlock || is.getItem() instanceof ItemSkull))
                WikiTools.getInstance().getEntity().replaceItemInInventory(103, is);
            else if (is.getItem() instanceof ItemArmor)
                WikiTools.getInstance().getEntity().replaceItemInInventory(100 + (3 - ((ItemArmor) is.getItem()).armorType), is);
            else
                WikiTools.getInstance().getEntity().setCurrentItemOrArmor(0, is);

            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(I18n.format("wikitools.message.addedItem")));
        }
    }

    public String sanitiseAll(String text, boolean delete, boolean ui) {
        text = text.replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("&", "\\\\&")
                .replaceAll("\\|", "{{!}}")
                .replaceAll("/", "\\\\/");
        text = sanitiseColors(text, delete);

        if (!ui)
            // backslash handler when used in module context, e.g. for tooltip line
            // double backslash is read as single backslash in module
            text = text.replaceAll("\\\\", "\\\\\\\\");
            // then, handle single quotation marks
            text = text.replaceAll("'", "\\\\'");

        if (ui)
            // extra handlers when used in {{UI}} template context
            text = sanitiseCommas(text);

        return text;
    }
    public String sanitiseColors(String text, boolean delete) {
        // If not delete, replace all colour codes (§) with &
        if (!delete)
            text = text.replaceAll("\u00A7", "&");
        else
            text = text.replaceAll("\u00A7.", "");
        return text;
    }
    public String sanitiseCommas(String text) {
        // this is for UI lines only
        return text.replaceAll(",", "\\\\,");
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

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.entity == Minecraft.getMinecraft().thePlayer && !WikiTools.getInstance().updateMessage.isEmpty())
        {
            IChatComponent ichatcomponent = new ChatComponentText(WikiTools.getInstance().updateMessage);
            ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Charzard4261/wikitools/releases/latest"));
            ichatcomponent.getChatStyle().setUnderlined(true);
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(ichatcomponent);
            WikiTools.getInstance().updateMessage = "";
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderPlayerEvent.Pre event)
    {
        if (event.entity == WikiTools.getInstance().getEntity()
                && WikiTools.getInstance().getEntity() instanceof AbstractClientPlayer
                && !(event.renderer instanceof RenderPlayerOverride))
        {
            event.setCanceled(true);
            RenderPlayerOverride re = new RenderPlayerOverride(Minecraft.getMinecraft().getRenderManager(), WikiTools.getInstance().configs.smallArms);
            re.doRender((AbstractClientPlayer) event.entity, event.x, event.y, event.z, WikiTools.getInstance().configs.bodyYaw, 0);
        }
    }

}
