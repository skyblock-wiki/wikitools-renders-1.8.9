package org.hsw.wikitoolsrenders.feature.remind_mod_update;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import net.minecraft.util.*;
import org.hsw.wikitoolsrenders.WikiToolsRendersIdentity;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
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

    private static Optional<String> findNewVersion() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(WikiToolsRendersIdentity.RELEASES_QUERY_URL);
            request.addHeader("content-type", "application/json");

            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");

            Release release = new Gson().fromJson(json, Release.class);
            String latestVersionName = release.tag_name;

            if (release.tag_name == null) {
                warnFailure("Latest Release Fetch/Parse Failure (" + result.getStatusLine() + ")");
                return Optional.empty();  // request failure
            }

            boolean modNeedsUpdating = checkIfModNeedsUpdating(latestVersionName);
            if (!modNeedsUpdating) {
                return Optional.empty();
            }

            return Optional.of(latestVersionName);
        } catch (IOException ignored) {
            warnFailure("Latest Release Fetch Failure");
            return Optional.empty();
        }
    }

    private static boolean checkIfModNeedsUpdating(String latestVersionName) {
        // This assumes the current version to be any valid version using SemVer
        // (Correct: 2.0.0, 2.0.0-beta.1)

        // This assumes the latest version to have no prerelease tag
        // (Correct: v2.0.0; Incorrect: v2.0.0-beta.1)

        // Hence we assume no case where the latest version is a higher prerelease version
        // and which we do not want to remind users of.

        String currentVersionName = WikiToolsRendersIdentity.VERSION;

        Optional<Version> currentVersion = getVersion(currentVersionName);
        Optional<Version> latestVersion = getVersion(latestVersionName);

        if (!currentVersion.isPresent() || !latestVersion.isPresent()) {
            if (!currentVersion.isPresent()) {
                warnFailure("Version Parse Failure (" + currentVersionName + ")");
            }
            if (!latestVersion.isPresent()) {
                warnFailure("Version Parse Failure (" + latestVersionName + ")");
            }
            return false;
        }

        return latestVersion.get().isHigherThan(currentVersion.get());
    }

    private static Optional<Version> getVersion(String versionName) {
        // For version names like "v2.0.0",
        // remove "v" from the start of string
        if (versionName.startsWith("v")) {
            versionName = versionName.substring(1);
        }

        return Version.tryParse(versionName);
    }

    private static class Release {
        public String tag_name;
    }

}
