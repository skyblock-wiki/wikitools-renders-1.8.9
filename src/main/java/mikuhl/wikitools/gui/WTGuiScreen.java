package mikuhl.wikitools.gui;

import mikuhl.wikitools.WikiTools;
import mikuhl.wikitools.entity.EntityRenderClone;
import mikuhl.wikitools.helper.FramebufferHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static jdk.nashorn.internal.objects.Global.Infinity;

public class WTGuiScreen extends GuiScreen implements GuiPageButtonList.GuiResponder, GuiSlider.FormatHelper {

    public ResourceLocation uiImages = new ResourceLocation(WikiTools.MODID, "ui_components.png");
    boolean rendering = false;

    @Override
    public void initGui()
    {
        super.initGui();

        ScaledResolution res = new ScaledResolution(mc);
        int sw = res.getScaledWidth();
        int sh = res.getScaledHeight();

        int anchorX = sw / 2;
        int anchorY = sh / 2;
        int height = 250;
        int width = 400;
        int offset = 6;

        WTGuiButton steve = new WTGuiButton(4261,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - 54) / 2,
                100, 20,
                I18n.format("wikitools.gui.setSteve"));
        buttonList.add(steve);

        WTGuiButton invisible = new WTGuiButton(4262,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - 10 - 40 - 54) / 2,
                100, 20,
                I18n.format("wikitools.gui.toggleInvisible"));
        buttonList.add(invisible);

        WTGuiButton remove_enchants = new WTGuiButton(4263,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 + -(10 + 40) * 2 - 54) / 2,
                100, 20,
                I18n.format("wikitools.gui.removeEnchants"));
        buttonList.add(remove_enchants);

        WTGuiButton remove_armour = new WTGuiButton(4264,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 3 - 54) / 2,
                100, 20,
                I18n.format("wikitools.gui.removeArmour"));
        buttonList.add(remove_armour);

        WTGuiButton remove_item = new WTGuiButton(4265,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 4 - 54) / 2,
                100, 20,
                I18n.format("wikitools.gui.removeItem"));
        //remove_item.enabled = false;
        buttonList.add(remove_item);

        GuiSlider headPitch = new GuiSlider(this, 4266,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 5 - 54) / 2,
                I18n.format("wikitools.gui.headPitch"), -90.0f, 90.0f, 0.0f, this);
        buttonList.add(headPitch);

        GuiSlider headYaw = new GuiSlider(this, 4267,
                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 6 - 54) / 2,
                I18n.format("wikitools.gui.headYaw"), -90.0f, 90.0f, 0.0f, this);
        buttonList.add(headYaw);

        WTGuiButton toggle_small_arms = new WTGuiButton(4268,
                anchorX - (width - offset - 14 - 10 - (256 * 2)) / 2, anchorY - (height - offset - 14 - 54) / 2,
                100, 20,
                I18n.format("wikitools.gui.toggleSmallArms"));
        buttonList.add(toggle_small_arms);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        ScaledResolution res = new ScaledResolution(mc);
        int sw = res.getScaledWidth();
        int sh = res.getScaledHeight();

        drawDefaultBackground();

        int anchorX = sw / 2;
        int anchorY = sh / 2;
        int height = 250;
        int width = 400;
        int offset = 6;

        // Draw Outer Trim
        drawRect(anchorX - width / 2, anchorY - height / 2,
                anchorX + width / 2, anchorY + height / 2, 0x9933187E);
        // Draw Trim
        drawRect(anchorX - (width - offset) / 2 - 1, anchorY - (height - offset) / 2 - 1,
                anchorX + (width - offset) / 2 + 1, anchorY + (height - offset) / 2 + 1, 0xFF7110CC);
        // Draw Main Panel
        drawRect(anchorX - (width - offset) / 2, anchorY - (height - offset) / 2,
                anchorX + (width - offset) / 2, anchorY + (height - offset) / 2, 0xFF33187E);
        // Draw Nameplate
        drawRect(anchorX - (width - offset - 14) / 2, anchorY - (height - offset - 14) / 2,
                anchorX + (-width + offset + 16 + 260) / 2, anchorY + (-height + offset + 14 + 30) / 2, 0xFF542DAF);
        drawCenteredString(mc.fontRendererObj, "Wikitools", anchorX - (width - offset - 14 - 130) / 2, anchorY - (height - offset - 14 - 8) / 2, 0xFF7053B2);

        // Draw Entity Box
        drawRect(anchorX - (width - offset - 20) / 2, anchorY - (height - offset - 20) / 2 + 24,
                anchorX - (width - offset - 20) / 2 + 125, anchorY + (height - offset - 20) / 2, 0xFF000000);

        // Gotta call this or skin is completely black
        FramebufferHelper.drawEntityOnScreen(0, 0, 0, WikiTools.getInstance().getEntity());
        // Check if holding item, if so, move to the right a bit
        FramebufferHelper.drawEntityOnScreen(anchorX - (width - offset - 20 - 124) / 2, anchorY + (height - offset - 64) / 2, 90, WikiTools.getInstance().getEntity());

        // Hacky method so that Ghasts don't just cover over UI buttons
        // For some reason you can't copy Ender Dragons so it applies for Ghasts
        // Should probably check for Giants?
        if (WikiTools.getInstance().getEntity() instanceof EntityGhast)
        {
            zLevel = 300;
            for (GuiButton bt : buttonList)
                if (bt instanceof WTGuiButton)
                    ((WTGuiButton) bt).setZLevel(600);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(uiImages);
        // Draw Save Image Button
        DrawButton(anchorX + (width - offset - 32 - 14) / 2, anchorY - (height - offset - 14) / 2, mouseX, mouseY, 0, 0, 16, 16);
        // Draw Render Head Button
        DrawButton(anchorX + (width - offset - 32 * 2 - 4 - 14) / 2, anchorY - (height - offset - 14) / 2, mouseX, mouseY, 16, 0, 16, 16);
        // Draw Render Skin Button
        DrawButton(anchorX + (width - offset - 32 * 3 - 4 * 2 - 14) / 2, anchorY - (height - offset - 14) / 2, mouseX, mouseY, 48, 0, 16, 16);
        // Draw Copy Self Button
        DrawButton(anchorX + (width - offset - 32 * 4 - 4 * 3 - 14) / 2, anchorY - (height - offset - 14) / 2, mouseX, mouseY, 32, 0, 16, 16);

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (WikiTools.getInstance().getEntity() instanceof EntityGhast)
        {
            zLevel = 0;
            for (GuiButton bt : buttonList)
                if (bt instanceof WTGuiButton)
                    ((WTGuiButton) bt).setZLevel(0);
        }
    }

    public void DrawButton(int x, int y, int mouseX, int mouseY, int sourceX, int sourceY, int width, int height)
    {
        if (CheckButton(x, y, width, height, mouseX, mouseY)) // Hovered
            drawTexturedModalRect(x, y, sourceX, sourceY + width, width, height);
        else // Default
            drawTexturedModalRect(x, y, sourceX, sourceY, width, height);

    }

    public boolean CheckButton(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return (x <= mouseX && mouseX < x + width &&
                y <= mouseY && mouseY < y + height);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        ScaledResolution res = new ScaledResolution(mc);
        int sw = res.getScaledWidth();
        int sh = res.getScaledHeight();

        int anchorX = sw / 2;
        int anchorY = sh / 2;
        int height = 250;
        int width = 400;
        int offset = 6;

        // Check Save Image Button
        if (CheckButton(anchorX + (width - offset - 32 - 14) / 2, anchorY - (height - offset - 14) / 2, 16, 16, mouseX, mouseY))
        {
            if (rendering) return;

            rendering = true;
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

            int displayWidth = mc.displayWidth;
            int displayHeight = mc.displayHeight;
            int shortest = Math.min(Math.min(displayWidth, displayHeight), 512);

            Framebuffer framebuffer = FramebufferHelper.createFrameBuffer(displayWidth, displayHeight);

            float scale = 1;
            BufferedImage bufferedImage = renderEntity(0, scale, WikiTools.getInstance().getEntity(), framebuffer);
            int longest = getLongest(bufferedImage);

            if (longest != 0)
            {
                // Some mobs require extremely fine scaling to equal exactly our size.
                // Be ok with 1 pixel smaller if it cant find the exact scale fast enough.
                while (longest != shortest && longest != shortest - 1)
                {
                    scale = shortest / (longest / scale);
                    if (scale == Infinity) break;
                    bufferedImage = renderEntity(shortest, scale, WikiTools.getInstance().getEntity(), framebuffer);
                    longest = getLongest(bufferedImage);
                }

                FramebufferHelper.saveBuffer(bufferedImage);
                FramebufferHelper.restoreFrameBuffer(framebuffer);
            }

            rendering = false;
        } // Check Render Head Button
        else if (CheckButton(anchorX + (width - offset - 32 * 2 - 4 - 14) / 2, anchorY - (height - offset - 14) / 2, 16, 16, mouseX, mouseY))
        {
            if (rendering) return;

            if (WikiTools.getInstance().getEntity() instanceof AbstractClientPlayer)
            {
                try
                {
                    rendering = true;
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

                    ResourceLocation rl = ((AbstractClientPlayer) WikiTools.getInstance().getEntity()).getLocationSkin();
                    ThreadDownloadImageData dat = (ThreadDownloadImageData) Minecraft.getMinecraft().getTextureManager().getTexture(rl);
                    BufferedImage bufferedImage = ReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, dat, "bufferedImage", "field_110560_d");
                    BufferedImage icon = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
                    Graphics2D G2D = icon.createGraphics();
                    G2D.drawImage(bufferedImage.getSubimage(8, 8, 8, 8), 0, 0, null);
                    G2D.drawImage(bufferedImage.getSubimage(40, 8, 8, 8), 0, 0, null);
                    G2D.dispose();

                    BufferedImage newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = newImage.createGraphics();
                    //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.clearRect(0, 0, 64, 64);
                    g.drawImage(icon, 0, 0, 64, 64, null);
                    g.dispose();

                    FramebufferHelper.saveBuffer(newImage);

                    rendering = false;
                } catch (Exception e)
                {
                    System.out.println(e);
                    rendering = false;
                }
            }
        } // Check Render Skin Button
        else if (CheckButton(anchorX + (width - offset - 32 * 3 - 4 * 2 - 14) / 2, anchorY - (height - offset - 14) / 2, 16, 16, mouseX, mouseY))
        {
            if (rendering) return;

            if (WikiTools.getInstance().getEntity() instanceof AbstractClientPlayer)
            {
                rendering = true;
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

                ResourceLocation rl = ((AbstractClientPlayer) WikiTools.getInstance().getEntity()).getLocationSkin();
                ThreadDownloadImageData dat = (ThreadDownloadImageData) Minecraft.getMinecraft().getTextureManager().getTexture(rl);
                BufferedImage bufferedImage = ReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, dat, "bufferedImage", "field_110560_d");
                FramebufferHelper.saveBuffer(bufferedImage);
                rendering = false;
            }
        } // Check Copy Self Button
        else if (CheckButton(anchorX + (width - offset - 32 * 4 - 4 * 3 - 14) / 2, anchorY - (height - offset - 14) / 2, 16, 16, mouseX, mouseY))
        {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            WikiTools.getInstance().setEntity(new EntityRenderClone(Minecraft.getMinecraft().thePlayer, false));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id == 4261) // Set to Steve
        {
            if (WikiTools.getInstance().getEntity() instanceof AbstractClientPlayer)
                WikiTools.getInstance().setEntity(new EntityRenderClone((AbstractClientPlayer) WikiTools.getInstance().getEntity(), true));
        } else if (button.id == 4262) // Toggle Invisible
        {
            WikiTools.getInstance().getEntity().setInvisible(!WikiTools.getInstance().getEntity().isInvisible());
        } else if (button.id == 4263) // Remove Enchants
        {
            for (ItemStack itemStack : WikiTools.getInstance().getEntity().getInventory())
                if (itemStack != null && itemStack.hasTagCompound())
                    itemStack.getTagCompound().removeTag("ench");

            if (WikiTools.getInstance().getEntity().getHeldItem() != null && WikiTools.getInstance().getEntity().getHeldItem().hasTagCompound())
                WikiTools.getInstance().getEntity().getHeldItem().getTagCompound().removeTag("ench");
        } else if (button.id == 4264) // Remove Armour
        {
            try
            {
                WikiTools.getInstance().getEntity().replaceItemInInventory(100, null);
                WikiTools.getInstance().getEntity().replaceItemInInventory(101, null);
                WikiTools.getInstance().getEntity().replaceItemInInventory(102, null);
                WikiTools.getInstance().getEntity().replaceItemInInventory(103, null);
            } catch (Exception e)
            {
                System.out.println(e);
            }
        } else if (button.id == 4265) // Remove Held Item
        {
            if (WikiTools.getInstance().getEntity() instanceof AbstractClientPlayer)
                for (int i = 0; i < 9; i++)
                    WikiTools.getInstance().getEntity().replaceItemInInventory(0, null);
            else
                WikiTools.getInstance().getEntity().replaceItemInInventory(99, null);
        } else if (button.id == 4268)
        {
            WikiTools.getInstance().configs.smallArms = !WikiTools.getInstance().configs.smallArms;
        }
    }

    private BufferedImage renderEntity(int height, float scale, EntityLivingBase entity, Framebuffer framebuffer)
    {

        FramebufferHelper.clearFrameBuffer();

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int posX = resolution.getScaledWidth() / 2;
        int posY = resolution.getScaledHeight() / 2 + (height / resolution.getScaleFactor() / 2);

        FramebufferHelper.drawEntityOnScreen(posX, posY, scale, entity);
        BufferedImage readImage = FramebufferHelper.readImage(framebuffer);
        BufferedImage trimImage = FramebufferHelper.trimImage(readImage);

        return trimImage;
    }

    private int getLongest(BufferedImage image)
    {
        return Math.max(image.getWidth(), image.getHeight());
    }

    @Override
    public void func_175321_a(int p_175321_1_, boolean p_175321_2_)
    {

    }

    @Override
    public void onTick(int id, float value)
    {
        if (id == 4266)
        {
            WikiTools.getInstance().configs.headPitch = (int) value;
            WikiTools.getInstance().setEntity(WikiTools.getInstance().getEntity());
        } else if (id == 4267)
        {
            WikiTools.getInstance().configs.headYaw = (int) value;
            WikiTools.getInstance().setEntity(WikiTools.getInstance().getEntity());
        }
    }

    @Override
    public void func_175319_a(int p_175319_1_, String p_175319_2_)
    {

    }

    @Override
    public String getText(int id, String name, float value)
    {
        return name + ": " + (int) value;
    }
}
