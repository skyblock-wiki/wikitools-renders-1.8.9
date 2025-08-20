package org.hsw.wikitoolsrenders.feature.render_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import static jdk.nashorn.internal.objects.Global.Infinity;

class EntityRenderer {

    private static boolean renderingInProgress = false;

    private static Optional<RenderableEntity> currentEntity = Optional.empty();

    private static RenderableEntity getCurrentEntity() {
        if (!currentEntity.isPresent()) {
            setCurrentEntity(ClonedClientPlayer.of(Minecraft.getMinecraft().thePlayer));
        }
        return currentEntity.get();
    }

    public static boolean currentEntityIsPlayerEntity() {
        return currentEntity.isPresent() && currentEntity.get().isPlayerEntity();
    }

    private static Optional<ResourceLocation> getSkinOfCurrentEntity() {
        if (!currentEntity.isPresent()) {
            return Optional.empty();
        }

        return currentEntity.get().getCustomSkin();
    }

    private static Optional<BufferedImage> getSkinImageOfCurrentEntity() {
        Optional<ResourceLocation> skin = getSkinOfCurrentEntity();
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();

        if (!skin.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(TextureUtil.readBufferedImage(
                    resourceManager.getResource(skin.get()).getInputStream()
            ));
        }
        catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static RenderableEntity.EntityControlState getCurrentEntityControlState() {
        return getCurrentEntity().getControlState();
    }

    public static void setCurrentEntity(EntityLivingBase entity) {
        RenderableEntity renderableEntity = new RenderableEntity(entity);
        currentEntity = Optional.of(renderableEntity);
    }

    public static void saveEntityImage(int displayWidth, int displayHeight) {
        startRenderingProcess(() -> {
            int shortest = Math.min(Math.min(displayWidth, displayHeight), 512);

            Framebuffer framebuffer = FrameBufferHelper.createFrameBuffer(displayWidth, displayHeight);

            float scale = 1;
            BufferedImage bufferedImage = renderEntity(0, scale, framebuffer);
            int longestSide = getLongestSide(bufferedImage);

            if (longestSide != 0) {
                // Some mobs require extremely fine scaling to equal exactly our size.
                // Be ok with 1 pixel smaller if it cant find the exact scale fast enough.
                while (longestSide != shortest && longestSide != shortest - 1) {
                    scale = shortest / (longestSide / scale);
                    if (scale == Infinity) {
                        break;
                    }
                    bufferedImage = renderEntity(shortest, scale, framebuffer);
                    longestSide = getLongestSide(bufferedImage);
                }

                FrameBufferHelper.saveBuffer(bufferedImage);
                FrameBufferHelper.restoreFrameBuffer(framebuffer);
            }
        });
    }

    private static BufferedImage renderEntity(int height, float scale, Framebuffer framebuffer) {
        FrameBufferHelper.clearFrameBuffer();

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int posX = resolution.getScaledWidth() / 2;
        int posY = resolution.getScaledHeight() / 2 + (height / resolution.getScaleFactor() / 2);

        drawCurrentEntityOnScreen(posX, posY, scale);
        BufferedImage readImage = FrameBufferHelper.readImage(framebuffer);
        BufferedImage trimImage = FrameBufferHelper.trimImage(readImage);

        return trimImage;
    }

    public static void drawCurrentEntityOnScreen(int posX, int posY, float scale) {
        getCurrentEntity().drawOnScreen(posX, posY, scale);
    }

    private static int getLongestSide(BufferedImage image) {
        return Math.max(image.getWidth(), image.getHeight());
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
            newImageG2D.fillRect(0,0,finalLayerSize,finalLayerSize);

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
        }
        catch (Exception e) {
            renderingInProgress = false;

            throw e;
        }
    }

}
