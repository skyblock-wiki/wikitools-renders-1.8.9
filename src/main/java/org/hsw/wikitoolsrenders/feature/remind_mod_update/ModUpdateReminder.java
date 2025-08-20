package org.hsw.wikitoolsrenders.feature.remind_mod_update;

import com.google.gson.Gson;
import org.hsw.wikitoolsrenders.WikiToolsRendersInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Optional;

public class ModUpdateReminder {

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity == Minecraft.getMinecraft().thePlayer) {
            Optional<String> newVersionName = findNewVersion();
            newVersionName.ifPresent(ModUpdateReminder::remindUserToUpdateMod);
        }
    }

    private static void remindUserToUpdateMod(String updateMessage) {
        IChatComponent ichatcomponent = new ChatComponentText(
                I18n.format("wikitoolsrenders.needsUpdating") + " " + updateMessage);
        ichatcomponent.getChatStyle().setChatClickEvent(
                new ClickEvent(ClickEvent.Action.OPEN_URL, WikiToolsRendersInfo.REPOSITORY_URL));
        ichatcomponent.getChatStyle().setUnderlined(true);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(ichatcomponent);
    }

    private static Optional<String> findNewVersion() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(WikiToolsRendersInfo.RELEASES_QUERY_URL);
            request.addHeader("content-type", "application/json");

            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");

            Release release = new Gson().fromJson(json, Release.class);
            String latestVersionName = release.tag_name;

            if (release.tag_name == null) {
                return Optional.empty();  // request failure
            }

            boolean modNeedsUpdating = checkIfModNeedsUpdating(latestVersionName);
            if (!modNeedsUpdating) {
                return Optional.empty();
            }

            return Optional.of(latestVersionName);
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private static boolean checkIfModNeedsUpdating(String latestVersionName) {
        return !latestVersionName.equals(WikiToolsRendersInfo.VERSION);
    }

    private static class Release {
        public String tag_name;
    }

}
