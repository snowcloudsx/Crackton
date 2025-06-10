package me.snow.gui.widgets;

import net.minecraft.client.gui.DrawContext;

public abstract class Component {
    public abstract void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta);
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public void mouseReleased(double mouseX, double mouseY, int button) { }
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) { }
    public int getHeight() { return 20; }
}