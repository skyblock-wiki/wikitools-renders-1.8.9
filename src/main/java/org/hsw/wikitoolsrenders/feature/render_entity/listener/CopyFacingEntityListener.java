package org.hsw.wikitoolsrenders.feature.render_entity.listener;

import org.hsw.wikitoolsrenders.WikiToolsRendersKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.hsw.wikitoolsrenders.feature.render_entity.render.ClonedClientPlayer;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;

import java.util.Optional;

public class CopyFacingEntityListener {

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        if (!WikiToolsRendersKeybinds.COPY_ENTITY.isKeyDown()) {
            return;
        }

        copyFacingEntityToGui();
    }

    private static void copyFacingEntityToGui() {
        Minecraft minecraft = Minecraft.getMinecraft();
        MovingObjectPosition objectMouseOver = minecraft.objectMouseOver;

        if (objectMouseOver == null) {
            return;
        }

        Entity facingEntity = objectMouseOver.entityHit;

        if (facingEntity == null) {
            return;
        }

        Optional<EntityLivingBase> livingEntity = getLivingEntity(facingEntity);

        if (!livingEntity.isPresent()) {
            return;
        }

        EntityRenderer.setCurrentEntity(livingEntity.get());

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
                new ChatComponentText(I18n.format("wikitoolsrenders.message.copiedEntity")));
    }

    private static Optional<EntityLivingBase> getLivingEntity(Entity entity) {
        Minecraft minecraft = Minecraft.getMinecraft();

        // Get the right entity
        if (entity instanceof EntityLivingBase) {
            if (entity instanceof EntityOtherPlayerMP) {
                return Optional.of(ClonedClientPlayer.of((EntityOtherPlayerMP) entity));
            } else {
                NBTTagCompound nbt = entity.serializeNBT();
                Entity entityFromNBT = EntityList.createEntityFromNBT(nbt, minecraft.theWorld);
                return Optional.of((EntityLivingBase) entityFromNBT);
            }
        } else if (WikiToolsRendersKeybinds.SELF_MODIFIER.isKeyDown()) {
            return Optional.of(ClonedClientPlayer.of(minecraft.thePlayer));
        }

        return Optional.empty();
    }

}
