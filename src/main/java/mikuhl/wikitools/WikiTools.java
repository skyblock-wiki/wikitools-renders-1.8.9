package mikuhl.wikitools;

import mikuhl.wikitools.helper.FramebufferHelper;
import mikuhl.wikitools.listeners.RenderListener;
import mikuhl.wikitools.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.awt.image.BufferedImage;

import static jdk.nashorn.internal.objects.Global.Infinity;

@Mod(modid = WikiTools.MODID, version = WikiTools.VERSION)
public class WikiTools {
    public static final String MODID   = "wikitools";
    public static final String VERSION = "1.0";

    private static WikiTools instance;
    public WikiToolsConfigs configs;

    public RenderListener   renderListener;
    public EntityLivingBase entity = null;

    public WikiTools()
    {
        instance = this;
    }

    @SidedProxy(serverSide = "mikuhl.wikitools.proxy.CommonProxy", clientSide = "mikuhl.wikitools.proxy.ClientProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        configs = new WikiToolsConfigs();
    }

    public static WikiTools getInstance()
    {
        return instance;
    }

    public EntityLivingBase getEntity()
    {
        return entity;
    }

    public void setEntity(EntityLivingBase entity)
    {
        this.entity = entity;

        // Disable red glow
        entity.hurtTime = 0;

        // Disable name tag
        entity.posX = Double.MAX_VALUE;
        entity.posY = Double.MAX_VALUE;
        entity.posZ = Double.MAX_VALUE;

        // Set rotation
        entity.renderYawOffset = configs.bodyYaw.getValue();
        entity.prevRenderYawOffset = configs.bodyYaw.getValue();
        entity.rotationYaw = 0;
        // Head rotation pitch
        entity.rotationPitch = configs.headPitch.getValue();
        entity.rotationYawHead = configs.bodyYaw.getValue() + configs.headYaw.getValue();
        entity.prevRotationYawHead = configs.bodyYaw.getValue() + configs.headYaw.getValue();
    }

    boolean rendering = false;

    public void render()
    {
        if (rendering) return;

        Minecraft minecraft = Minecraft.getMinecraft();

        rendering = true;
        int displayWidth = minecraft.displayWidth;
        int displayHeight = minecraft.displayHeight;
        int shortest = Math.min(Math.min(displayWidth, displayHeight), 512);

        Framebuffer framebuffer = FramebufferHelper.createFrameBuffer(displayWidth, displayHeight);

        float scale = 1;
        BufferedImage bufferedImage = renderEntity(0, scale, entity, framebuffer);
        int longest = getLongest(bufferedImage);

        if (longest != 0)
        {
            // Some mobs require extremely fine scaling to equal exactly our size.
            // Be ok with 1 pixel smaller if it cant find the exact scale fast enough.
            while (longest != shortest && longest != shortest - 1)
            {
                scale = shortest / (longest / scale);
                if (scale == Infinity) break;
                bufferedImage = renderEntity(shortest, scale, entity, framebuffer);
                longest = getLongest(bufferedImage);
            }

            FramebufferHelper.saveBuffer(bufferedImage);
            FramebufferHelper.restoreFrameBuffer(framebuffer);
        }

        rendering = false;
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
}
