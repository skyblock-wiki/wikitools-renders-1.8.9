package org.hsw.wikitoolsrenders.feature.render_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

class RenderableEntity {
    private static final boolean specialRotation = true;

    private static int customSkinCounter = 0;

    private final EntityLivingBase entity;

    private final Optional<ResourceLocation> customSkin;

    private boolean smallArms = false;
    private float headPitch = 0.0f;
    private float headYaw = 0.0f;
    private final float bodyYaw = 0.0f;

    public RenderableEntity(EntityLivingBase entity) {
        this.entity = entity;

        this.customSkin = findCustomSkin();

        if (isPlayerEntity()) {
            AbstractClientPlayer playerEntity = (AbstractClientPlayer) entity;

            configurePlayerEntity(playerEntity);
        }
    }

    public boolean isPlayerEntity() {
        return entity instanceof AbstractClientPlayer;
    }

    private Optional<ItemStack> getEntityInventorySlot(int i) {
        ItemStack[] inventory = entity.getInventory();
        if (i < inventory.length) {
            return Optional.ofNullable(inventory[i]);
        }
        return Optional.empty();
    }

    private Optional<AbstractClientPlayer> getAsPlayerEntity() {
        if (!isPlayerEntity()) {
            return Optional.empty();
        }

        AbstractClientPlayer playerEntity = (AbstractClientPlayer) entity;
        return Optional.of(playerEntity);
    }

    public Optional<ResourceLocation> getCustomSkin() {
        return customSkin;
    }

    private Optional<ResourceLocation> findCustomSkin() {
        if (!isPlayerEntity()) {
            return Optional.empty();
        }

        AbstractClientPlayer playerEntity = (AbstractClientPlayer) entity;

        ResourceLocation steveTexture = DefaultPlayerSkin.getDefaultSkin(playerEntity.getUniqueID());
        boolean entityHasSteveSkin = steveTexture.equals(playerEntity.getLocationSkin());
        boolean entityHasCustomSkin = playerEntity.hasSkin() || !entityHasSteveSkin;

        if (!entityHasCustomSkin) {
            return Optional.of(steveTexture);
        }

        ResourceLocation customTexture = createCustomTexture(playerEntity, "WikiToolsCustomSkin_" + customSkinCounter);
        customSkinCounter++;

        return Optional.ofNullable(customTexture);
    }

    private void configurePlayerEntity(AbstractClientPlayer playerEntity) {
        smallArms = playerEntity.getSkinType().equalsIgnoreCase("slim");

        // Disable red glow
        playerEntity.hurtTime = 0;

        // Disable name tag
        playerEntity.posX = Double.MAX_VALUE;
        playerEntity.posY = Double.MAX_VALUE;
        playerEntity.posZ = Double.MAX_VALUE;

        // Set rotation
        playerEntity.renderYawOffset = bodyYaw;
        playerEntity.prevRenderYawOffset = bodyYaw;
        playerEntity.rotationYaw = 0;

        // Head rotation pitch
        playerEntity.rotationPitch = headPitch;
        playerEntity.prevRotationPitch = headPitch;
        playerEntity.rotationYawHead = bodyYaw + headYaw;
        playerEntity.prevRotationYawHead = bodyYaw + headYaw;
    }

    private static ResourceLocation createCustomTexture(AbstractClientPlayer abstractClientPlayer, String saveAsName) {
        ResourceLocation skin = abstractClientPlayer.getLocationSkin();
        ThreadDownloadImageData imageData = (ThreadDownloadImageData) Minecraft.getMinecraft().getTextureManager().getTexture(skin);
        BufferedImage bufferedImage = ReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, imageData, "bufferedImage", "field_110560_d");

        Graphics2D G2D = bufferedImage.createGraphics();

        for (int x = 0; x < bufferedImage.getWidth(); x++)
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                Color c = new Color(bufferedImage.getRGB(x, y), true);
                if (0 < c.getAlpha() && c.getAlpha() < 255) {
                    Color corrected = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
                    G2D.setColor(corrected);
                    G2D.fill(new Rectangle(x, y, 1, 1));
                }
            }
        G2D.dispose();

        DynamicTexture dynTex = new DynamicTexture(bufferedImage);
        return Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation(saveAsName, dynTex);
    }

    public EntityControlState getControlState() {
        return new EntityControlState(smallArms, headPitch, headYaw, bodyYaw);
    }

    public void drawOnScreen(int posX, int posY, float scale) {
        FrameBufferHelper.drawEntityOnScreen(posX, posY, scale, entity, specialRotation);
    }

    public void setToSteve() {
        Optional<AbstractClientPlayer> playerEntity = getAsPlayerEntity();
        playerEntity.ifPresent(abstractClientPlayer -> EntityRenderer.setCurrentEntity(ClonedClientPlayer.asSteve(abstractClientPlayer)));
    }

    public void toggleInvisibility() {
        entity.setInvisible(!entity.isInvisible());
    }

    public boolean inventoryHasEnchantedItem() {
        for (ItemStack itemStack : entity.getInventory()) {
            if (itemStack != null && itemStack.isItemEnchanted()) {
                return true;
            }
        }

        ItemStack heldItem = entity.getHeldItem();
        if (heldItem != null && heldItem.isItemEnchanted()) {
            return true;
        }

        return false;
    }

    public void removeEnchantsInInventory() {
        for (ItemStack itemStack : entity.getInventory()) {
            if (itemStack != null && itemStack.hasTagCompound()) {
                itemStack.getTagCompound().removeTag("ench");
            }
        }

        ItemStack heldItem = entity.getHeldItem();
        if (heldItem != null && heldItem.hasTagCompound()) {
            heldItem.getTagCompound().removeTag("ench");
        }
    }

    public boolean hasHeldItem() {
        ItemStack heldItem = entity.getHeldItem();
        return heldItem != null;
    }

    public void removeHeldItem() {
        entity.setCurrentItemOrArmor(0, null);
    }

    public boolean hasArmourPieces() {
        return entity.getCurrentArmor(0) != null ||
                entity.getCurrentArmor(1) != null ||
                entity.getCurrentArmor(2) != null ||
                entity.getCurrentArmor(3) != null;
    }

    public void removeArmourPieces() {
        entity.setCurrentItemOrArmor(1, null);
        entity.setCurrentItemOrArmor(2, null);
        entity.setCurrentItemOrArmor(3, null);
        entity.setCurrentItemOrArmor(4, null);
    }

    public void toggleSmallArms() {
        smallArms = !smallArms;
    }

    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
        getAsPlayerEntity().ifPresent(this::configurePlayerEntity);
    }

    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
        getAsPlayerEntity().ifPresent(this::configurePlayerEntity);
    }

    public boolean equalsToEntity(Entity entity) {
        return this.entity == entity;
    }

    public void doCustomRender(double x, double y, double z) {
        if (!getAsPlayerEntity().isPresent()) {
            return;
        }

        CustomPlayerRenderer customPlayerRenderer = new CustomPlayerRenderer(Minecraft.getMinecraft().getRenderManager(), customSkin, smallArms);
        customPlayerRenderer.doRender(getAsPlayerEntity().get(), x, y, z, bodyYaw, 0);
    }

    public static class CustomPlayerRenderer extends RenderPlayer {
        private final Optional<ResourceLocation> skin;

        public CustomPlayerRenderer(RenderManager renderManager, Optional<ResourceLocation> skin, boolean useSmallArms) {
            super(renderManager, useSmallArms);
            this.skin = skin;
        }

        @Override
        protected ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
            return skin.orElseGet(entity::getLocationSkin);
        }
    }

    public static class EntityControlState {
        public final boolean smallArms;
        public final float headPitch;
        public final float headYaw;
        public final float bodyYaw;

        public EntityControlState(boolean smallArms, float headPitch, float headYaw, float bodyYaw) {
            this.smallArms = smallArms;
            this.headPitch = headPitch;
            this.headYaw = headYaw;
            this.bodyYaw = bodyYaw;
        }
    }
}
