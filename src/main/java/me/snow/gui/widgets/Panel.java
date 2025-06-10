package me.snow.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class Panel {
    private final String title;
    private final List<Component> components;
    private int x, y;
    private final int width = 180;
    private int height;
    private boolean dragging = false;
    private int offsetX, offsetY;

    public Panel(String title, int x, int y, List<Component> components) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.components = components;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        height = 25 + components.stream().mapToInt(Component::getHeight).sum() + (components.size() - 1) * 4;
        ctx.fill(x, y, x + width, y + height, 0xFF1A1A1A);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, title, x + 6, y + 6, 0xFFFFFFAA, false);

        int yOffset = y + 25;
        for (Component c : components) {
            c.render(ctx, x + 5, yOffset, mouseX, mouseY, delta);
            yOffset += c.getHeight() + 4;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
            dragging = true;
            offsetX = (int) mouseX - x;
            offsetY = (int) mouseY - y;
            return true;
        }
        int yOffset = y + 25;
        for (Component c : components) {
            if (c.mouseClicked(mouseX - x - 5, mouseY - yOffset, button)) return true;
            yOffset += c.getHeight() + 4;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;

        // Forward release events to ALL components
        int yOffset = y + 25;
        for (Component c : components) {
            c.mouseReleased(mouseX - x - 5, mouseY - yOffset, button);
            yOffset += c.getHeight() + 4;
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            x = (int) mouseX - offsetX;
            y = (int) mouseY - offsetY;
        } else {
            // Forward drag events to ALL components
            int yOffset = y + 25;
            for (Component c : components) {
                c.mouseDragged(mouseX - x - 5, mouseY - yOffset, button, dx, dy);
                yOffset += c.getHeight() + 4;
            }
        }
    }
}