package org.hsw.wikitoolsrenders.feature.render_entity.render;

import com.google.common.io.Files;
import org.hsw.wikitoolsrenders.WikiToolsRendersInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import scala.Tuple4;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Date;

class FrameBufferHelper {

    private static Framebuffer framebuffer;

    public static Framebuffer createFrameBuffer(int width, int height) {
        framebuffer = Minecraft.getMinecraft().getFramebuffer();
        Framebuffer framebuffer = new Framebuffer(width, height, true);
        framebuffer.bindFramebuffer(true);
        clearFrameBuffer();
        return framebuffer;
    }

    public static void clearFrameBuffer() {
        GlStateManager.clearColor(0, 0, 0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public static void restoreFrameBuffer(Framebuffer toDelete) {
        toDelete.deleteFramebuffer();
        if (framebuffer != null) {
            framebuffer.bindFramebuffer(true);
        } else {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        }
    }

    public static BufferedImage readImage(Framebuffer framebuffer) {
        int width = framebuffer.framebufferWidth;
        int height = framebuffer.framebufferHeight;
        IntBuffer pixels = BufferUtils.createIntBuffer(width * height);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
        int[] vals = new int[width * height];
        pixels.get(vals);
        TextureUtil.processPixelValues(vals, width, height);
        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedimage.setRGB(0, 0, width, height, vals, 0, width);
        return bufferedimage;
    }

    public static class TrimmedImage {
        public final BufferedImage trimmedImage;
        public final int horizontalBalance;  // negative if left-heavy, positive if right-heavy
        public final int verticalBalance;  // negative if top-heavy, positive if bottom-heavy

        public TrimmedImage(BufferedImage originalImage) {

            Tuple4<Integer, Integer, Integer, Integer> trimBox = getTrimBox(originalImage);
            int left = trimBox._1();
            int top = trimBox._2();
            int right = trimBox._3();
            int bottom = trimBox._4();
            int width = right - left + 1;
            int height = bottom - top + 1;

            this.trimmedImage = originalImage.getSubimage(left, top, width, height);

            horizontalBalance = (originalImage.getWidth() - right - left) / 2;
            verticalBalance = (originalImage.getHeight() - bottom - top) / 2;
        }

        private static Tuple4<Integer, Integer, Integer, Integer> getTrimBox(BufferedImage image) {
            WritableRaster raster = image.getAlphaRaster();
            int width = raster.getWidth();
            int height = raster.getHeight();
            int left = 0;
            int top = 0;
            int right = width - 1;
            int bottom = height - 1;
            int minRight = width - 1;
            int minBottom = height - 1;

            top:
            for (; top < bottom; top++) {
                for (int x = 0; x < width; x++) {
                    if (raster.getSample(x, top, 0) != 0) {
                        minRight = x;
                        minBottom = top;
                        break top;
                    }
                }
            }

            left:
            for (; left < minRight; left++) {
                for (int y = height - 1; y > top; y--) {
                    if (raster.getSample(left, y, 0) != 0) {
                        minBottom = y;
                        break left;
                    }
                }
            }

            bottom:
            for (; bottom > minBottom; bottom--) {
                for (int x = width - 1; x >= left; x--) {
                    if (raster.getSample(x, bottom, 0) != 0) {
                        minRight = x;
                        break bottom;
                    }
                }
            }

            right:
            for (; right > minRight; right--) {
                for (int y = bottom; y >= top; y--) {
                    if (raster.getSample(right, y, 0) != 0) {
                        break right;
                    }
                }
            }

            return new Tuple4<>(left, top, right, bottom);
        }
    }

    public static void saveBuffer(BufferedImage bufferedImage) {
        try {
            File f = new File(WikiToolsRendersInfo.MODID + "/", new Date().getTime() + ".png");
            Files.createParentDirs(f);
            f.createNewFile();
            ImageIO.write(bufferedImage, "png", f);
            IChatComponent ichatcomponent = new ChatComponentText(f.getName());
            ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, f.getParent()));
            ichatcomponent.getChatStyle().setUnderlined(true);
            ChatComponentTranslation success = new ChatComponentTranslation("screenshot.success", ichatcomponent);
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(success);
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static void drawEntityOnScreen(int posX, int posY, float scale, EntityLivingBase entity, boolean doSpecialRotation) {
        GlStateManager.pushMatrix();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableTexture2D();
        GlStateManager.enableRescaleNormal();

        //GlStateManager.enableDepth();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();

        if (doSpecialRotation) {
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, -1.0F, 0.0F);
            GlStateManager.rotate(30.0F, 1.0F, 0.0F, -1.0F);
            GlStateManager.translate(0.0F, 0.0F, 0.0F);
        }

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        boolean oldShadows = rendermanager.isRenderShadow();
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(entity, 0, 0, 0, 0, 1.0F);
        // Fix colored things like sheep, and leather armor.
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rendermanager.setRenderShadow(oldShadows);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.popMatrix();
    }

}
