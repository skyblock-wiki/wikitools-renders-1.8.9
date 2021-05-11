package mikuhl.wikitools.handler;

import mikuhl.wikitools.WikiTools;
import mikuhl.wikitools.WikiToolsKeybinds;
import mikuhl.wikitools.entity.EntityRenderClone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class EntityRenderHandler {

    private static boolean rendering = false;

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event)
    {
        if (WikiToolsKeybinds.HUD.isKeyDown())
        {
            WikiTools.getInstance().renderListener.openUI = true;
            return;
        }

        if (!WikiToolsKeybinds.RENDER.isKeyDown()) return;

        if (rendering) return;

        Minecraft minecraft = Minecraft.getMinecraft();

        MovingObjectPosition objectMouseOver = minecraft.objectMouseOver;
        Entity entityHit = objectMouseOver.entityHit;

        // Get the right entity
        EntityLivingBase entity = null;
        if (entityHit instanceof EntityLivingBase)
        {
            if (entityHit instanceof EntityOtherPlayerMP)
            {
                entity = new EntityRenderClone(((EntityOtherPlayerMP) entityHit), false);
            } else
            {
                NBTTagCompound nbt = entityHit.serializeNBT();
                entity = ((EntityLivingBase) EntityList.createEntityFromNBT(nbt, minecraft.theWorld));
            }
        } else if (WikiToolsKeybinds.SELF_MODIFIER.isKeyDown())
        {
            entity = new EntityRenderClone(minecraft.thePlayer, false);
        }

        if (entity == null) return;

        WikiTools.getInstance().setEntity(entity);

    }

}
