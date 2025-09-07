package org.hsw.wikitoolsrenders.feature.remind_mod_update;

import net.minecraft.util.*;
import org.hsw.wikitoolsrenders.WikiToolsRendersIdentity;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModUpdateReminder {

    private final GetNewVersionHandler getNewVersionHandler;

    public ModUpdateReminder(GetNewVersionHandler getNewVersionHandler) {
        this.getNewVersionHandler = getNewVersionHandler;
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity != Minecraft.getMinecraft().thePlayer) {
            return;
        }

        String currentVersionName = WikiToolsRendersIdentity.VERSION;
        GetNewVersionHandler.GetNewVersionResponse response =
                getNewVersionHandler.getNewVersion(
                        new GetNewVersionHandler.GetNewVersionRequest(currentVersionName));

        if (!response.success || !response.result.isPresent()) {
            warnFailure(response.message.orElse("Unknown error"));
            return;
        }

        if (response.result.get().hasNewRelease) {
            remindUserToUpdateMod(response.result.get().latestVersion);
        }
    }

    private static void remindUserToUpdateMod(String newVersionName) {
        IChatComponent frontComponent = new ChatComponentTranslation("wikitoolsrenders.remindModUpdate.needsUpdating", newVersionName)
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));

        ChatStyle linkStyle = new ChatStyle()
                .setColor(EnumChatFormatting.YELLOW)
                .setUnderlined(true)
                .setChatClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL, WikiToolsRendersIdentity.REPOSITORY_URL));

        IChatComponent linkComponent = new ChatComponentTranslation("wikitoolsrenders.remindModUpdate.updateLinkText")
                .setChatStyle(linkStyle);

        IChatComponent messageComponent = new ChatComponentText("")
                .appendSibling(frontComponent)
                .appendText(" ")
                .appendSibling(linkComponent);

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(messageComponent);
    }

    private static void warnFailure(String problemName) {
        WikiToolsRendersIdentity.getLogger().warn(
                "FAILURE (Mod Update Reminder): {}", problemName);
    }

}
