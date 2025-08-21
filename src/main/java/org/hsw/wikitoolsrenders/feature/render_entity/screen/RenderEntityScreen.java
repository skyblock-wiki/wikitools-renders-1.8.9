package org.hsw.wikitoolsrenders.feature.render_entity.screen;

import net.minecraft.client.renderer.GlStateManager;
import org.hsw.wikitoolsrenders.WikiToolsRendersInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.hsw.wikitoolsrenders.WikiToolsRendersKeybinds;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;
import org.hsw.wikitoolsrenders.feature.render_entity.render.RenderableEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RenderEntityScreen extends GuiScreen implements GuiPageButtonList.GuiResponder, GuiSlider.FormatHelper {

    private List<NormalButton> normalButtons;
    private List<NormalSlider> normalSliders;
    private List<IconButton> iconButtons;

    private final ResourceLocation uiComponentsImage = new ResourceLocation(WikiToolsRendersInfo.MODID, "ui_components.png");

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        // Handle exit GUI
        if (WikiToolsRendersKeybinds.HUD.getKeyCode() == keyCode) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        initializeGuiElements();

        registerGuiElements();
    }

    public void initializeGuiElements() {
        ScaledResolution res = new ScaledResolution(mc);
        final int sw = res.getScaledWidth();
        final int sh = res.getScaledHeight();

        final int anchorX = sw / 2;
        final int anchorY = sh / 2;
        final int height = 250;
        final int width = 400;
        final int offset = 6;

        final int toggleSmallArmsButtonId = 4261;
        final int toggleInvisibleButtonId = 4262;
        final int removeEnchantsButtonId = 4263;
        final int removeArmourButtonId = 4264;
        final int removeHeldItemButtonId = 4265;
        final int headPitchSliderId = 4266;
        final int headYawSliderId = 4267;

        this.normalButtons = new ArrayList<>(Arrays.asList(
                new NormalButton(
                        new GuiButton(toggleSmallArmsButtonId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - 54) / 2,
                                100, 20,
                                I18n.format("wikitoolsrenders.gui.toggleSmallArms")),
                        NormalButton.WHEN_ENTITY_IS_PLAYER,
                        EntityRenderer::toggleSmallArms
                ),
                new NormalButton(
                        new GuiButton(toggleInvisibleButtonId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - 10 - 40 - 54) / 2,
                                100, 20,
                                I18n.format("wikitoolsrenders.gui.toggleInvisible")),
                        NormalButton.ALWAYS,
                        EntityRenderer::toggleEntityInvisibility
                ),
                new NormalButton(
                        new GuiButton(removeEnchantsButtonId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 + -(10 + 40) * 2 - 54) / 2,
                                100, 20,
                                I18n.format("wikitoolsrenders.gui.removeEnchants")),
                        EntityRenderer::canRemoveEnchantsInEntityInventory,
                        EntityRenderer::removeEnchantsInEntityInventory
                ),
                new NormalButton(
                        new GuiButton(removeArmourButtonId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 3 - 54) / 2,
                                100, 20,
                                I18n.format("wikitoolsrenders.gui.removeArmour")),
                        EntityRenderer::canRemoveArmourPiecesOfEntity,
                        EntityRenderer::removeArmourPiecesOfEntity
                ),
                new NormalButton(
                        new GuiButton(removeHeldItemButtonId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 4 - 54) / 2,
                                100, 20,
                                I18n.format("wikitoolsrenders.gui.removeItem")),
                        EntityRenderer::canRemoveHeldItemOfEntity,
                        EntityRenderer::removeHeldItemOfEntity
                )
        ));

        RenderableEntity.EntityControlState controlState = EntityRenderer.getCurrentEntityControlState();

        this.normalSliders = new ArrayList<>(Arrays.asList(
                new NormalSlider(
                        -90.0f, 90.0f,
                        new GuiSlider(this, headPitchSliderId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 5 - 54) / 2,
                                I18n.format("wikitoolsrenders.gui.headPitch"), -90.0f, 90.0f, controlState.headPitch, this),
                        NormalButton.WHEN_ENTITY_IS_PLAYER,
                        EntityRenderer::setHeadPitch
                ),
                new NormalSlider(
                        -90.0f, 90.0f,
                        new GuiSlider(this, headYawSliderId,
                                anchorX - (width - offset - 14 - 10 - 256) / 2, anchorY - (height - offset - 14 - (10 + 40) * 6 - 54) / 2,
                                I18n.format("wikitoolsrenders.gui.headYaw"), -90.0f, 90.0f, controlState.headYaw, this),
                        NormalButton.WHEN_ENTITY_IS_PLAYER,
                        EntityRenderer::setHeadYaw
                )
        ));

        this.iconButtons = new ArrayList<>(Arrays.asList(
                new IconButton(
                        I18n.format("wikitoolsrenders.gui.saveEntityImage"),
                        new IconButton.IconButtonConfig(anchorX + (width - offset - 32 - 14) / 2, anchorY - (height - offset - 14) / 2, 0, 0, 16, 16),
                        this::drawIconButton,
                        IconButton.ALWAYS,
                        () -> {
                            makeButtonClickSound();
                            EntityRenderer.saveEntityImage(mc.displayWidth, mc.displayHeight);
                        }
                ),
                new IconButton(
                        I18n.format("wikitoolsrenders.gui.downloadHead"),
                        new IconButton.IconButtonConfig(anchorX + (width - offset - 32 * 2 - 4 - 14) / 2, anchorY - (height - offset - 14) / 2, 16, 0, 16, 16),
                        this::drawIconButton,
                        IconButton.WHEN_ENTITY_IS_PLAYER,
                        () -> {
                            makeButtonClickSound();
                            EntityRenderer.downloadHead();
                        }
                ),
                new IconButton(
                        I18n.format("wikitoolsrenders.gui.downloadSkin"),
                        new IconButton.IconButtonConfig(anchorX + (width - offset - 32 * 3 - 4 * 2 - 14) / 2, anchorY - (height - offset - 14) / 2, 16 * 3, 0, 16, 16),
                        this::drawIconButton,
                        IconButton.WHEN_ENTITY_IS_PLAYER,
                        () -> {
                            makeButtonClickSound();
                            EntityRenderer.downloadSkin();
                        }
                ),
                new IconButton(
                        I18n.format("wikitoolsrenders.gui.copySelf"),
                        new IconButton.IconButtonConfig(anchorX + (width - offset - 32 * 4 - 4 * 3 - 14) / 2, anchorY - (height - offset - 14) / 2, 16 * 2, 0, 16, 16),
                        this::drawIconButton,
                        IconButton.ALWAYS,
                        () -> {
                            makeButtonClickSound();
                            EntityRenderer.setEntityToCurrentPlayer();
                            resetSliders();
                        }
                ),
                new IconButton(
                        I18n.format("wikitoolsrenders.gui.setSteve"),
                        new IconButton.IconButtonConfig(anchorX + (width - offset - 32 * 5 - 4 * 4 - 14) / 2, anchorY - (height - offset - 14) / 2, 16 * 4, 0, 16, 16),
                        this::drawIconButton,
                        IconButton.ALWAYS,
                        () -> {
                            makeButtonClickSound();
                            EntityRenderer.setEntityToSteve();
                            resetSliders();
                        }
                )
        ));
    }

    private void resetSliders() {
        for (NormalSlider normalSlider : normalSliders) {
            normalSlider.setToZero();
        }
    }

    private void registerGuiElements() {
        for (NormalButton normalButton : normalButtons) {
            buttonList.add(normalButton.guiButton);
        }

        for (NormalSlider normalSlider : normalSliders) {
            buttonList.add(normalSlider.guiSlider);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution res = new ScaledResolution(mc);
        int sw = res.getScaledWidth();
        int sh = res.getScaledHeight();

        drawDefaultBackground();

        int anchorX = sw / 2;
        int anchorY = sh / 2;
        int height = 250;
        int width = 400;
        int offset = 6;

        // Depth is disabled to organize elements from bottom to top
        GlStateManager.disableDepth();

        drawGuiBackground(anchorX, anchorY, width, height, offset);

        drawIconButtonDescription(anchorX, anchorY, width, height, offset, mouseX, mouseY);

        {
            // Depth is temporarily re-enabled since entity renderer draws with depth
            GlStateManager.enableDepth();

            // Gotta call this or skin is completely black
            EntityRenderer.drawCurrentEntityOnScreen(0, 0, 0);
            // Check if holding item, if so, move to the right a bit
            EntityRenderer.drawCurrentEntityOnScreen(anchorX - (width - offset - 20 - 124) / 2, anchorY + (height - offset - 64) / 2, 90);

            GlStateManager.disableDepth();
        }

        drawIconButtons(mouseX, mouseY);

        for (NormalButton normalButton : normalButtons) {
            normalButton.prepare();
        }

        for (NormalSlider normalSlider : normalSliders) {
            normalSlider.prepare();
        }

        // This will draw the normal buttons and labels
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Depth is re-enabled after drawing our elements
        GlStateManager.enableDepth();
    }

    private void drawGuiBackground(int anchorX, int anchorY, int width, int height, int offset) {
        // Draw Outer Trim
        drawRect(anchorX - width / 2, anchorY - height / 2,
                anchorX + width / 2, anchorY + height / 2, 0xFF000000);

        // Draw Corners
        drawRect(anchorX - (width - offset) / 2 - 2, anchorY - (height - offset) / 2 - 2,
                anchorX + (width - offset) / 2 + 2, anchorY + (height - offset) / 2 + 2, 0xFF8B8B8B);

        // Draw T+L Trim
        drawRect(anchorX - (width - offset) / 2 - 2, anchorY - (height - offset) / 2 - 2,
                anchorX + (width - offset) / 2, anchorY + (height - offset) / 2, 0xFFFFFFFF);

        // Draw B+R Trim
        drawRect(anchorX - (width - offset) / 2, anchorY - (height - offset) / 2,
                anchorX + (width - offset) / 2 + 2, anchorY + (height - offset) / 2 + 2, 0xFF555555);

        // Draw Main Panel
        drawRect(anchorX - (width - offset) / 2, anchorY - (height - offset) / 2,
                anchorX + (width - offset) / 2, anchorY + (height - offset) / 2, 0xFFC6C6C6);

        // Draw Entity Box
        drawRect(anchorX - (width - offset - 20) / 2, anchorY - (height - offset - 20) / 2 + 24,
                anchorX - (width - offset - 20) / 2 + 125, anchorY + (height - offset - 20) / 2, 0xFF000000);

        // Draw Nameplate
        drawRect(anchorX - (width - offset - 14) / 2, anchorY - (height - offset - 14) / 2,
                anchorX - (width - offset - 14 - 248 - 14) / 2, anchorY - (height - offset - 14 - 30) / 2, 0xFF6D6D6D);
        drawCenteredString(mc.fontRendererObj, "Wikitools Renders", anchorX - (width - offset - 14 - 124 - 6) / 2, anchorY - (height - offset - 14 - 8) / 2, 0xFFE0E0E0);
    }

    private void drawIconButtonDescription(int anchorX, int anchorY, int width, int height, int offset, int mouseX, int mouseY) {
        Optional<String> hoveredIconButtonDescription = Optional.empty();

        for (IconButton iconButton : iconButtons) {
            Optional<String> name = iconButton.elementNameOnHover(mouseX, mouseY);
            if (name.isPresent()) {
                hoveredIconButtonDescription = name;
            }
        }

        if (hoveredIconButtonDescription.isPresent()) {
            int stringWidth = mc.fontRendererObj.getStringWidth(hoveredIconButtonDescription.get());

            int actualPredictedStringWidth = stringWidth + 7;

            int x = anchorX - (width - offset - 14 - 10 - 256) / 2;
            int y = anchorY - (height - offset - 14 - (10 + 40) * 7 - 54) / 2;

            drawRect(x, y, x + actualPredictedStringWidth + 7, y + 15, 0xAA6D6431);

            drawString(mc.fontRendererObj, hoveredIconButtonDescription.get(), x + 7, y + 4, 0xFFE0E0E0);
        }
    }

    private void drawIconButtons(int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(uiComponentsImage);

        for (IconButton iconButton : iconButtons) {
            iconButton.drawElement(mouseX, mouseY);
        }
    }

    private void drawIconButton(IconButton.IconButtonConfig config) {
        drawTexturedModalRect(config.x, config.y, config.sourceX, config.sourceY, config.width, config.height);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (IconButton iconButton : iconButtons) {
            iconButton.elementClicked(mouseX, mouseY);
        }
    }

    private void makeButtonClickSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        for (NormalButton normalButton : normalButtons) {
            normalButton.elementClicked(button.id);
        }
    }

    @Override
    public void onTick(int id, float value) {
        for (NormalSlider normalSlider : normalSliders) {
            normalSlider.elementClicked(id, value);
        }
    }

    @Override
    public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {
    }

    @Override
    public void func_175319_a(int p_175319_1_, String p_175319_2_) {
    }

    @Override
    public String getText(int id, String name, float value) {
        return name + ": " + (int) value;
    }

}
