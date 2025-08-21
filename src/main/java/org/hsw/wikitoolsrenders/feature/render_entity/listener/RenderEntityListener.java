package org.hsw.wikitoolsrenders.feature.render_entity.listener;

import org.hsw.wikitoolsrenders.WikiToolsRendersKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;
import org.hsw.wikitoolsrenders.feature.render_entity.screen.RenderEntityScreen;

public class RenderEntityListener {

    private static boolean toOpenGui = false;

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        if (!WikiToolsRendersKeybinds.HUD.isKeyDown()) {
            return;
        }

        toOpenGui = true;
    }

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (toOpenGui) {
            Minecraft.getMinecraft().displayGuiScreen(new RenderEntityScreen());
        }

        toOpenGui = false;
    }

    @SubscribeEvent
    public void onRenderLiving(RenderPlayerEvent.Pre event) {
        boolean isHandled = EntityRenderer.ensurePlayerEntityInGuiIsCustomRendered(
                event.entity,
                event.renderer,
                event.x,
                event.y,
                event.z
        );

        if (isHandled) {
            event.setCanceled(true);
        }
    }

}
