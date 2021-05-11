package mikuhl.wikitools.gui;

import net.minecraft.client.gui.GuiButton;

public class WTGuiButton extends GuiButton {
    public WTGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void setZLevel(float i)
    {
        zLevel = i;
    }
}
