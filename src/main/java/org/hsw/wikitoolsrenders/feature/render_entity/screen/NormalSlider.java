package org.hsw.wikitoolsrenders.feature.render_entity.screen;

import net.minecraft.client.gui.GuiSlider;
import org.hsw.wikitoolsrenders.feature.render_entity.render.EntityRenderer;

import java.util.function.Consumer;
import java.util.function.Supplier;

class NormalSlider {
    public static final Supplier<Boolean> WHENEVER = () -> true;

    public static final Supplier<Boolean> WHEN_ENTITY_IS_PLAYER = EntityRenderer::currentEntityIsPlayerEntity;

    private final float minValue;
    private final float maxValue;
    public final GuiSlider guiSlider;

    private final Supplier<Boolean> useConditionIsMet;

    private final Consumer<Float> executeAction;

    public NormalSlider(float minValue, float maxValue, GuiSlider guiSlider, Supplier<Boolean> useConditionIsMet, Consumer<Float> executeAction) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.guiSlider = guiSlider;
        this.useConditionIsMet = useConditionIsMet;
        this.executeAction = executeAction;
    }

    public void prepare() {
        guiSlider.enabled = useConditionIsMet.get();
    }

    public void elementClicked(int idOfElementClicked, float newValue) {
        if (!(idOfElementClicked == guiSlider.id)) {
            return;
        }

        if (!useConditionIsMet.get()) {
            return;
        }

        executeAction.accept(newValue);
    }

    public void setToZero() {
        float fractionalPositionOfZero = (0 - minValue) / (maxValue - minValue);
        guiSlider.func_175219_a(fractionalPositionOfZero);
    }
}
