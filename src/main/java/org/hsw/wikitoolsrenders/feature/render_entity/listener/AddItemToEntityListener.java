package org.hsw.wikitoolsrenders.feature.render_entity.listener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.hsw.wikitoolsrenders.WikiToolsRendersKeybinds;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;
import org.lwjgl.input.Keyboard;

import java.util.Optional;

public class AddItemToEntityListener {

    @SubscribeEvent
    public void copyEntityHandler(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        boolean mIsPressed = Keyboard.isKeyDown(WikiToolsRendersKeybinds.COPY_ENTITY.getKeyCode());
        boolean mIsPressedWithShift = mIsPressed && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        if (mIsPressed && mIsPressedWithShift) {
            addHoveredItemAsArmor(event.gui);
        }
        else if (mIsPressed) {
            addHoveredItemAsHeldItem(event.gui);
        }
    }

    private Optional<ItemStack> getHoveredItemStack(GuiScreen guiScreen) {
        if (!(guiScreen instanceof GuiContainer)) {
            return Optional.empty();
        }

        GuiContainer guiContainer = (GuiContainer) guiScreen;

        if (guiContainer.getSlotUnderMouse() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(guiContainer.getSlotUnderMouse().getStack());
    }

    private void addHoveredItemAsHeldItem(GuiScreen guiScreen) {
        Optional<ItemStack> itemStack = getHoveredItemStack(guiScreen);

        if (!itemStack.isPresent()) {
            return;  // Cannot find hovered item stack
        }

        EntityRenderer.setEntityHeldItem(itemStack.get());

        String tipInfo = I18n.format("wikitoolsrenders.addItemToEntity.destinationTip.heldItem") + "\n" +
                I18n.format("wikitoolsrenders.addItemToEntity.destinationTip.armorPiece");
        IChatComponent destinationTip = new ChatComponentText("(◕‿◕)").setChatStyle(
                new ChatStyle().setChatHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(tipInfo))));
        IChatComponent outputText = new ChatComponentText(I18n.format("wikitoolsrenders.addItemToEntity.successMessage")).appendText("\n")
                .appendText("└ ").appendText(I18n.format("wikitoolsrenders.addItemToEntity.asHeldItem")).appendText(" ").appendSibling(destinationTip);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(outputText);
    }

    private void addHoveredItemAsArmor(GuiScreen guiScreen) {
        Optional<ItemStack> itemStack = getHoveredItemStack(guiScreen);

        if (!itemStack.isPresent()) {
            return;  // Cannot find hovered item stack
        }

        boolean success = EntityRenderer.setEntityArmorPiece(itemStack.get());

        String tipInfo = I18n.format("wikitoolsrenders.addItemToEntity.destinationTip.heldItem") + "\n" +
                I18n.format("wikitoolsrenders.addItemToEntity.destinationTip.armorPiece");
        IChatComponent destinationTip = new ChatComponentText("(◕‿◕)").setChatStyle(
                new ChatStyle().setChatHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(tipInfo))));
        String message = success ? I18n.format("wikitoolsrenders.addItemToEntity.successMessage") : I18n.format("wikitoolsrenders.addItemToEntity.failureMessage");
        IChatComponent outputText = new ChatComponentText(message).appendText("\n")
                .appendText("└ ").appendText(I18n.format("wikitoolsrenders.addItemToEntity.asArmorPiece")).appendText(" ").appendSibling(destinationTip);
        if (!success) {
            outputText.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
        }
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(outputText);
    }

}
