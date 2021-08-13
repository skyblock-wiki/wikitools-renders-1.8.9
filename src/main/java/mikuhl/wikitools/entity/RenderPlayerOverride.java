package mikuhl.wikitools.entity;

import mikuhl.wikitools.WikiTools;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;

public class RenderPlayerOverride extends RenderPlayer {
    public RenderPlayerOverride(RenderManager renderManager, boolean useSmallArms)
    {
        super(renderManager, useSmallArms);
    }

    @Override
    protected ResourceLocation getEntityTexture(AbstractClientPlayer entity)
    {
        return WikiTools.getInstance().currentCustomSkin != null ? WikiTools.getInstance().currentCustomSkin : entity.getLocationSkin();
    }

}
