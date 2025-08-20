package org.hsw.wikitoolsrenders;

import org.hsw.wikitoolsrenders.feature.render_entity.CopyFacingEntityListener;
import org.hsw.wikitoolsrenders.feature.render_entity.RenderEntityListener;
import org.hsw.wikitoolsrenders.feature.remind_mod_update.ModUpdateReminder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = WikiToolsRendersInfo.MODID, version = WikiToolsRendersInfo.VERSION, clientSideOnly = true)
public class WikiToolsRenders {

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ModUpdateReminder());
        MinecraftForge.EVENT_BUS.register(new CopyFacingEntityListener());
        MinecraftForge.EVENT_BUS.register(new RenderEntityListener());

        WikiToolsRendersKeybinds.init();
    }

}
