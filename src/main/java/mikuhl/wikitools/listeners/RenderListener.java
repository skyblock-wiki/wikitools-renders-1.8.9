package mikuhl.wikitools.listeners;

import mikuhl.wikitools.gui.WTGuiScreen;
import mikuhl.wikitools.WikiTools;
import mikuhl.wikitools.entity.EntityRenderClone;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RenderListener {
    public boolean openUI = false;
    
    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (openUI) {
            if (WikiTools.getInstance().getEntity() == null)
                WikiTools.getInstance().setEntity(new EntityRenderClone(Minecraft.getMinecraft().thePlayer, false));
            Minecraft.getMinecraft().displayGuiScreen(new WTGuiScreen());
        }
        openUI = false;
    }
}
