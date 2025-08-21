package org.hsw.wikitoolsrenders.feature.render_entity.screen;

import net.minecraft.client.gui.GuiButton;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;

import java.util.function.Supplier;

class NormalButton {
    public static final Supplier<Boolean> ALWAYS = () -> true;

    public static final Supplier<Boolean> WHEN_ENTITY_IS_PLAYER = EntityRenderer::currentEntityIsPlayerEntity;

    public final GuiButton guiButton;

    private final Supplier<Boolean> useConditionIsMet;

    private final Runnable executeAction;

    public NormalButton(GuiButton guiButton, Supplier<Boolean> useConditionIsMet, Runnable executeAction) {
        this.guiButton = guiButton;
        this.useConditionIsMet = useConditionIsMet;
        this.executeAction = executeAction;
    }

    public void prepare() {
        guiButton.enabled = useConditionIsMet.get();
    }

    public void elementClicked(int idOfElementClicked) {
        if (!(idOfElementClicked == guiButton.id)) {
            return;
        }

        if (!useConditionIsMet.get()) {
            return;
        }

        executeAction.run();
    }
}
