package org.hsw.wikitoolsrenders.feature.render_entity.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import static jdk.nashorn.internal.objects.Global.Infinity;

public class EntityRenderer {
    private static final int heightLimitForScreenRender = 200 - 80;
    private static final int widthLimitForScreenRender = 125 - 30;
    private static final int longestSideForIsometricImageRender = 512;

    private static boolean renderingInProgress = false;

    private static Optional<RenderableEntity> currentEntity = Optional.empty();

    private static Optional<EntityScale> currentEntityScaleForScreen = Optional.empty();

    private static RenderableEntity getCurrentEntity() {
        if (!currentEntity.isPresent()) {
            setCurrentEntity(ClonedClientPlayer.of(Minecraft.getMinecraft().thePlayer));
        }
        return currentEntity.get();
    }

    private static EntityScale getCurrentEntityScaleForScreen() {
        if (!currentEntityScaleForScreen.isPresent()) {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            int displayWidth = Minecraft.getMinecraft().displayWidth;
            int scaledWidth = scaledResolution.getScaledWidth();

            float scaleRatio = (float) displayWidth / scaledWidth;
            int width = (int) (widthLimitForScreenRender * scaleRatio);
            int height = (int) (heightLimitForScreenRender * scaleRatio);

            EntityScale entityScale = fitCurrentEntityToBox(width, height);
            currentEntityScaleForScreen = Optional.of(new EntityScale(
                    entityScale.width,
                    entityScale.height,
                    entityScale.scale,
                    (int) (entityScale.horizontalBalance / scaleRatio),
                    (int) (entityScale.verticalBalance / scaleRatio)
            ));
        }

        return currentEntityScaleForScreen.get();
    }

    public static boolean currentEntityIsPlayerEntity() {
        return currentEntity.isPresent() && currentEntity.get().isPlayerEntity();
    }

    private static Optional<ResourceLocation> getSkinOfCurrentEntity() {
        if (!currentEntity.isPresent()) {
            return Optional.empty();
        }

        return currentEntity.get().getPlayerAssociatedSkin();
    }

    private static Optional<BufferedImage> getSkinImageOfCurrentEntity() {
        Optional<ResourceLocation> skin = getSkinOfCurrentEntity();

        if (!skin.isPresent()) {
            return Optional.empty();
        }

        // ThreadDownloadImageData can be obtained with this method
        {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            ITextureObject textureObject = textureManager.getTexture(skin.get());

            if (textureObject instanceof ThreadDownloadImageData) {
                ThreadDownloadImageData imageData = (ThreadDownloadImageData) textureObject;
                BufferedImage bufferedImage = ReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, imageData, "bufferedImage", "field_110560_d");

                if (bufferedImage != null) {
                    return Optional.of(bufferedImage);
                }
            }
        }

        // SimpleTexture, however, requires this method
        {
            IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();

            try {
                return Optional.ofNullable(TextureUtil.readBufferedImage(
                        resourceManager.getResource(skin.get()).getInputStream()
                ));
            } catch (IOException ignored) {
                return Optional.empty();
            }
        }
    }

    public static RenderableEntity.EntityControlState getCurrentEntityControlState() {
        return getCurrentEntity().getControlState();
    }

    public static void setCurrentEntity(EntityLivingBase entity) {
        RenderableEntity renderableEntity = new RenderableEntity(entity);
        currentEntity = Optional.of(renderableEntity);
        currentEntityScaleForScreen = Optional.empty();
    }

    private static EntityScale fitCurrentEntityToSquareBox(int targetLongestSide) {
        int displayWidth = Minecraft.getMinecraft().displayWidth;
        int displayHeight = Minecraft.getMinecraft().displayHeight;

        Framebuffer framebuffer = FrameBufferHelper.createFrameBuffer(displayWidth, displayHeight);

        float scale = 90;
        BufferedImage bufferedImage = renderCurrentEntityToImage(0, scale, framebuffer).trimmedImage;
        int currentLongestSide = longestSide(bufferedImage);

        if (currentLongestSide == 0) {
            throw new RuntimeException("Unexpected length values when finding scale to render entity");
        }

        while (currentLongestSide != targetLongestSide && currentLongestSide != targetLongestSide - 1) {
            // scale_new / scale_current = length_target / length_current
            // there can be rounding errors, so it takes multiple iterations to find the fit
            scale = ((float) targetLongestSide / currentLongestSide) * scale;
            if (scale == Infinity) {
                break;
            }
            bufferedImage = renderCurrentEntityToImage(targetLongestSide, scale, framebuffer).trimmedImage;
            currentLongestSide = longestSide(bufferedImage);
        }

        FrameBufferHelper.TrimmedImage finalImage = renderCurrentEntityToImage(bufferedImage.getHeight(), scale, framebuffer);
        int width = finalImage.trimmedImage.getWidth();
        int height = finalImage.trimmedImage.getHeight();

        FrameBufferHelper.restoreFrameBuffer(framebuffer);

        return new EntityScale(width, height, scale, finalImage.horizontalBalance, finalImage.verticalBalance);
    }

    private static EntityScale fitCurrentEntityToBox(int widthLimit, int heightLimit) {
        EntityScale entityScale1 = fitCurrentEntityToSquareBox(widthLimit);
        EntityScale entityScale2 = fitCurrentEntityToSquareBox(heightLimit);

        boolean entityScale1IsValid = entityScale1.width <= widthLimit && entityScale1.height <= heightLimit;
        boolean entityScale2IsValid = entityScale2.width <= widthLimit && entityScale2.height <= heightLimit;
        boolean bothAreValid = entityScale1IsValid && entityScale2IsValid;

        if (entityScale1IsValid && !entityScale2IsValid) {
            return entityScale1;
        }

        if (!entityScale1IsValid && entityScale2IsValid) {
            return entityScale2;
        }

        EntityScale larger = entityScale1.scale > entityScale2.scale ? entityScale1 : entityScale2;
        EntityScale smaller = entityScale1.scale < entityScale2.scale ? entityScale1 : entityScale2;

        if (bothAreValid) {
            return larger;
        }

        // This extraordinary case should not happen
        return smaller;
    }

    private static int longestSide(BufferedImage bufferedImage) {
        return Math.max(bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    public static void saveEntityImage() {
        startRenderingProcess(() -> {
            EntityScale entityScale = fitCurrentEntityToSquareBox(longestSideForIsometricImageRender);

            int displayWidth = Minecraft.getMinecraft().displayWidth;
            int displayHeight = Minecraft.getMinecraft().displayHeight;

            Framebuffer framebuffer = FrameBufferHelper.createFrameBuffer(displayWidth, displayHeight);

            BufferedImage bufferedImage = renderCurrentEntityToImage(entityScale.height, entityScale.scale, framebuffer).trimmedImage;

            FrameBufferHelper.saveBuffer(bufferedImage);
            FrameBufferHelper.restoreFrameBuffer(framebuffer);
        });
    }

    private static FrameBufferHelper.TrimmedImage renderCurrentEntityToImage(int height, float scale, Framebuffer framebuffer) {
        FrameBufferHelper.clearFrameBuffer();

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int posX = resolution.getScaledWidth() / 2;
        int posY = resolution.getScaledHeight() / 2 + (height / resolution.getScaleFactor() / 2);

        drawCurrentEntityOnScreen(posX, posY, scale);
        BufferedImage readImage = FrameBufferHelper.readImage(framebuffer);

        return new FrameBufferHelper.TrimmedImage(readImage);
    }

    public static void drawCurrentEntityOnScreen(int posX, int posY) {
        EntityScale entityScale = getCurrentEntityScaleForScreen();
        drawCurrentEntityOnScreen(posX + entityScale.horizontalBalance, posY + entityScale.verticalBalance, entityScale.scale);
    }

    private static void drawCurrentEntityOnScreen(int posX, int posY, float scale) {
        getCurrentEntity().drawOnScreen(posX, posY, scale);
    }

    public static void downloadHead() {
        startRenderingProcess(() -> {
            Optional<BufferedImage> bufferedImage = getSkinImageOfCurrentEntity();

            if (!bufferedImage.isPresent()) {
                return;
            }

            int sourceSize = 8;

            // In java edition, outer layer is rendered 0.5 pixels larger than head layer (according to Minecraft wiki)
            // And the 0.5 pixels are added to all sides (according to observation)
            int headLayerSize = 64;
            int outerLayerSize = (int) (headLayerSize * ((sourceSize + 0.5 * 2) / sourceSize));
            int finalLayerSize = Math.max(headLayerSize, outerLayerSize);
            int headLayerOffset = (outerLayerSize - headLayerSize) / 2;
            int outerLayerOffset = 0;

            BufferedImage newImage = new BufferedImage(finalLayerSize, finalLayerSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D newImageG2D = newImage.createGraphics();

            // Fill with transparent pixels
            newImageG2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            newImageG2D.fillRect(0, 0, finalLayerSize, finalLayerSize);

            // Reset composite
            newImageG2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            // Draw
            newImageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            newImageG2D.drawImage(bufferedImage.get().getSubimage(8, 8, sourceSize, sourceSize), headLayerOffset, headLayerOffset, headLayerSize, headLayerSize, null);
            newImageG2D.drawImage(bufferedImage.get().getSubimage(40, 8, sourceSize, sourceSize), outerLayerOffset, outerLayerOffset, outerLayerSize, outerLayerSize, null);
            newImageG2D.dispose();

            FrameBufferHelper.saveBuffer(newImage);
        });
    }

    public static void downloadSkin() {
        startRenderingProcess(() -> {
            Optional<BufferedImage> bufferedImage = getSkinImageOfCurrentEntity();

            if (!bufferedImage.isPresent()) {
                return;
            }

            FrameBufferHelper.saveBuffer(bufferedImage.get());
        });
    }

    public static void setEntityToSteve() {
        if (!getCurrentEntity().isPlayerEntity()) {
            setEntityToCurrentPlayer();  // Set-to-steve requires a player entity
        }
        getCurrentEntity().setToSteve();
    }

    public static void setEntityToCurrentPlayer() {
        setCurrentEntity(ClonedClientPlayer.of(Minecraft.getMinecraft().thePlayer));
    }

    public static void toggleEntityInvisibility() {
        getCurrentEntity().toggleInvisibility();
    }

    public static boolean canRemoveEnchantsInEntityInventory() {
        return getCurrentEntity().inventoryHasEnchantedItem();
    }

    public static void removeEnchantsInEntityInventory() {
        getCurrentEntity().removeEnchantsInInventory();
    }

    public static boolean canRemoveHeldItemOfEntity() {
        return getCurrentEntity().hasHeldItem();
    }

    public static void removeHeldItemOfEntity() {
        getCurrentEntity().removeHeldItem();
    }

    public static boolean canRemoveArmourPiecesOfEntity() {
        return getCurrentEntity().hasArmourPieces();
    }

    public static void removeArmourPiecesOfEntity() {
        getCurrentEntity().removeArmourPieces();
    }

    public static void toggleSmallArms() {
        getCurrentEntity().toggleSmallArms();
    }

    public static void setHeadPitch(float headPitch) {
        getCurrentEntity().setHeadPitch(headPitch);
    }

    public static void setHeadYaw(float headYaw) {
        getCurrentEntity().setHeadYaw(headYaw);
    }

    public static boolean ensurePlayerEntityInGuiIsCustomRendered(Entity entity, RenderPlayer renderPlayer, double x, double y, double z) {
        boolean entityIsInGui = getCurrentEntity().equalsToEntity(entity);
        boolean isPlayerEntity = getCurrentEntity().isPlayerEntity();
        boolean entityDoesNotHaveCustomRender = !(renderPlayer instanceof RenderableEntity.CustomPlayerRenderer);

        boolean playerEntityInGuiDoesNotHaveCustomRenderer = entityIsInGui && isPlayerEntity && entityDoesNotHaveCustomRender;

        if (!playerEntityInGuiDoesNotHaveCustomRenderer) {
            return false;
        }

        getCurrentEntity().doCustomRender(x, y, z);

        return true;
    }

    private static void startRenderingProcess(Runnable func) {
        if (renderingInProgress) {
            return;
        }

        renderingInProgress = true;

        try {
            func.run();

            renderingInProgress = false;
        } catch (Exception e) {
            renderingInProgress = false;

            throw e;
        }
    }

    private static class EntityScale {
        public final int width;
        public final int height;
        public final float scale;
        public final int horizontalBalance;  // negative if left-heavy, positive if right-heavy
        public final int verticalBalance;  // negative if top-heavy, positive if bottom-heavy

        public EntityScale(int width, int height, float scale, int horizontalBalance, int verticalBalance) {
            this.width = width;
            this.height = height;
            this.scale = scale;
            this.horizontalBalance = horizontalBalance;
            this.verticalBalance = verticalBalance;
        }
    }

}
