package org.hsw.wikitoolsrenders;

import org.hsw.wikitoolsrenders.feature.remind_mod_update.GetNewVersionHandler;
import org.hsw.wikitoolsrenders.feature.remind_mod_update.GitHubLatestReleaseFinder;
import org.hsw.wikitoolsrenders.feature.render_entity.listener.AddItemToEntityListener;
import org.hsw.wikitoolsrenders.feature.render_entity.listener.CopyFacingEntityListener;
import org.hsw.wikitoolsrenders.feature.render_entity.listener.RenderEntityListener;
import org.hsw.wikitoolsrenders.feature.remind_mod_update.ModUpdateReminder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = WikiToolsRendersIdentity.MODID, version = WikiToolsRendersIdentity.VERSION, clientSideOnly = true)
public class WikiToolsRenders {

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(createModUpdateReminder());
        MinecraftForge.EVENT_BUS.register(new AddItemToEntityListener());
        MinecraftForge.EVENT_BUS.register(new CopyFacingEntityListener());
        MinecraftForge.EVENT_BUS.register(new RenderEntityListener());

        WikiToolsRendersKeybinds.init();
    }

    private static ModUpdateReminder createModUpdateReminder() {
        GitHubLatestReleaseFinder gitHubLatestReleaseFinder = new GitHubLatestReleaseFinder(
                WikiToolsRendersIdentity.RELEASES_QUERY_URL
        );
        GetNewVersionHandler getNewVersionHandler = new GetNewVersionHandler(gitHubLatestReleaseFinder);
        return new ModUpdateReminder(getNewVersionHandler);
    }

}
