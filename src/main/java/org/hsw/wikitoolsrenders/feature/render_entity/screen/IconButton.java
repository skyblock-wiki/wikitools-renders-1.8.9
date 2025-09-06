package org.hsw.wikitoolsrenders.feature.render_entity.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

class IconButton {
    public static final Supplier<Boolean> ALWAYS = () -> true;

    public static final Supplier<Boolean> WHEN_ENTITY_IS_PLAYER = EntityRenderer::currentEntityIsPlayerEntity;

    private final String name;

    private final IconButtonConfig iconButtonConfig;
    private final IconButtonConfig iconButtonConfigForHovered;
    private final IconButtonConfig iconButtonConfigForDisabled;

    private final Consumer<IconButtonConfig> onDraw;

    private final Supplier<Boolean> useConditionIsMet;

    private final Runnable executeAction;

    IconButton(
            String name,
            IconButtonConfig iconButtonConfig,
            Consumer<IconButtonConfig> onDraw,
            Supplier<Boolean> useConditionIsMet,
            Runnable executeAction
    ) {
        this.name = name;
        this.iconButtonConfig = iconButtonConfig;
        this.iconButtonConfigForHovered = getConfigForHovered(iconButtonConfig);
        this.iconButtonConfigForDisabled = getConfigForDisabled(iconButtonConfig);
        this.onDraw = onDraw;
        this.useConditionIsMet = useConditionIsMet;
        this.executeAction = executeAction;
    }

    private boolean buttonIsHoveredOver(int mouseX, int mouseY) {
        return (iconButtonConfig.x <= mouseX && mouseX < iconButtonConfig.x + iconButtonConfig.width &&
                iconButtonConfig.y <= mouseY && mouseY < iconButtonConfig.y + iconButtonConfig.height);
    }

    public void drawElement(int mouseX, int mouseY) {
        boolean drawAsDisabled = !useConditionIsMet.get();
        boolean drawAsHovered = !drawAsDisabled && buttonIsHoveredOver(mouseX, mouseY);

        if (drawAsDisabled) {
            onDraw.accept(iconButtonConfigForDisabled);
            return;
        }

        if (drawAsHovered) {
            onDraw.accept(iconButtonConfigForHovered);
            return;
        }

        onDraw.accept(iconButtonConfig);
    }

    public void elementClicked(int mouseX, int mouseY) {
        if (!(buttonIsHoveredOver(mouseX, mouseY))) {
            return;
        }

        if (!useConditionIsMet.get()) {
            return;
        }

        makeButtonClickSound();

        executeAction.run();
    }

    public Optional<String> elementNameOnHover(int mouseX, int mouseY) {
        if (!(buttonIsHoveredOver(mouseX, mouseY))) {
            return Optional.empty();
        }

        return Optional.of(name);
    }

    private static IconButtonConfig getConfigForHovered(IconButtonConfig config) {
        return new IconButtonConfig(
                config.x,
                config.y,
                config.sourceX,
                config.sourceY + config.height,
                config.width,
                config.height
        );
    }

    private static IconButtonConfig getConfigForDisabled(IconButtonConfig config) {
        return new IconButtonConfig(
                config.x,
                config.y,
                config.sourceX,
                config.sourceY + (2 * config.height),
                config.width,
                config.height
        );
    }

    public static class IconButtonConfig {
        public final int x;
        public final int y;
        public final int sourceX;
        public final int sourceY;
        public final int width;
        public final int height;

        public IconButtonConfig(int x, int y, int sourceX, int sourceY, int width, int height) {
            this.x = x;
            this.y = y;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.width = width;
            this.height = height;
        }
    }

    private void makeButtonClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(
                new ResourceLocation("gui.button.press"), 1.0F));
    }
}
