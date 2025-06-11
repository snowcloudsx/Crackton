package me.snow.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class Panel {
    private final String title;
    private final List<Component> components;
    private int x, y;
    private final int width = 180;
    private int height;
    private boolean dragging = false;
    private int offsetX, offsetY;

    // Animation
    private float hoverAnimation = 0f;
    private long lastTime = System.currentTimeMillis();
    private boolean isHovered = false;

    public Panel(String title, int x, int y, List<Component> components) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.components = components;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateAnimation(mouseX, mouseY);
        calculateHeight();

        // Modern layered background
        renderBackground(ctx);
        renderTitle(ctx);
        renderComponents(ctx, mouseX, mouseY, delta);
        renderAccents(ctx);
    }

    private void updateAnimation(int mouseX, int mouseY) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        if (isHovered && hoverAnimation < 1f) {
            hoverAnimation = Math.min(1f, hoverAnimation + deltaTime * 6f);
        } else if (!isHovered && hoverAnimation > 0f) {
            hoverAnimation = Math.max(0f, hoverAnimation - deltaTime * 6f);
        }
    }

    private void calculateHeight() {
        int componentHeight = components.stream().mapToInt(Component::getHeight).sum();
        int spacing = Math.max(0, components.size() - 1) * 6;
        height = 35 + componentHeight + spacing + 16;
    }

    private void renderBackground(DrawContext ctx) {
        // Base panel with glass effect
        ctx.fill(x, y, x + width, y + height, 0xF0080808);

        // Subtle gradient overlay
        for (int i = 0; i < height; i++) {
            float progress = (float) i / height;
            int alpha = (int)(15 * (1 - progress * 0.7f));
            int gradColor = (alpha << 24) | 0x00FFFFFF;
            ctx.fill(x, y + i, x + width, y + i + 1, gradColor);
        }

        // Border with hover glow
        int borderColor = interpolateColor(0x40FFFFFF, 0x80FFFFFF, hoverAnimation);
        drawBorder(ctx, x, y, width, height, borderColor);

        // Outer glow on hover
        if (hoverAnimation > 0) {
            int glowAlpha = (int)(hoverAnimation * 20);
            int glowColor = (glowAlpha << 24) | 0x00FFFFFF;
            drawBorder(ctx, x - 1, y - 1, width + 2, height + 2, glowColor);
        }
    }

    private void renderTitle(DrawContext ctx) {
        // Title bar background
        int titleBg = interpolateColor(0x20000000, 0x35000000, hoverAnimation);
        ctx.fill(x, y, x + width, y + 30, titleBg);

        // Title text - perfectly centered
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int titleWidth = textRenderer.getWidth(title);
        int titleX = x + (width - titleWidth) / 2;
        int titleY = y + 11;

        // Text with subtle glow
        int textColor = interpolateColor(0xFFCCCCCC, 0xFFFFFFFF, hoverAnimation * 0.5f);
        ctx.drawText(textRenderer, title, titleX, titleY, textColor, false);

        // Accent line under title
        int accentColor = interpolateColor(0x40FFFFFF, 0x70FFFFFF, hoverAnimation);
        ctx.fill(x + 8, y + 28, x + width - 8, y + 29, accentColor);
    }

    private void renderComponents(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int startY = y + 35;
        int contentX = x + 8;
        int contentWidth = width - 16;
        int yOffset = startY;

        for (Component component : components) {
            int compHeight = component.getHeight();

            // Component hover effect
            boolean compHovered = mouseX >= contentX && mouseX <= contentX + contentWidth &&
                    mouseY >= yOffset && mouseY <= yOffset + compHeight;

            if (compHovered) {
                int hoverBg = 0x15FFFFFF;
                ctx.fill(contentX - 2, yOffset - 2, contentX + contentWidth + 2, yOffset + compHeight + 2, hoverBg);

                // Subtle left accent
                ctx.fill(contentX - 2, yOffset - 2, contentX - 1, yOffset + compHeight + 2, 0x60FFFFFF);
            }

            // Center component horizontally
            int compWidth = getComponentWidth(component);
            int compX = contentX + (contentWidth - compWidth) / 2;

            component.render(ctx, compX, yOffset, mouseX, mouseY, delta);
            yOffset += compHeight + 6;
        }
    }

    private void renderAccents(DrawContext ctx) {
        // Top highlight
        int highlightColor = interpolateColor(0x15FFFFFF, 0x25FFFFFF, hoverAnimation);
        ctx.fill(x + 1, y + 1, x + width - 1, y + 2, highlightColor);

        // Side highlights
        ctx.fill(x + 1, y + 1, x + 2, y + height - 1, highlightColor);
        ctx.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, highlightColor);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.fill(x, y, x + width, y + 1, color); // Top
        ctx.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        ctx.fill(x, y, x + 1, y + height, color); // Left
        ctx.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    private int getComponentWidth(Component component) {
        // Try to get component width, fallback to content width
        try {
            var method = component.getClass().getMethod("getWidth");
            if (method.getReturnType() == int.class) {
                return (Integer) method.invoke(component);
            }
        } catch (Exception ignored) {}
        return width - 16; // Default to content width
    }

    private int interpolateColor(int color1, int color2, float progress) {
        progress = MathHelper.clamp(progress, 0f, 1f);

        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;

        int a = (int)(a1 + (a2 - a1) * progress);
        int r = (int)(r1 + (r2 - r1) * progress);
        int g = (int)(g1 + (g2 - g1) * progress);
        int b = (int)(b1 + (b2 - b1) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Drag from title bar only
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 30) {
            dragging = true;
            offsetX = (int) mouseX - x;
            offsetY = (int) mouseY - y;
            return true;
        }

        // Forward to components
        int startY = y + 35;
        int contentX = x + 8;
        int yOffset = startY;

        for (Component c : components) {
            int compWidth = getComponentWidth(c);
            int compX = contentX + (width - 16 - compWidth) / 2;

            if (c.mouseClicked(mouseX - compX, mouseY - yOffset, button)) return true;
            yOffset += c.getHeight() + 6;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;

        int startY = y + 35;
        int contentX = x + 8;
        int yOffset = startY;

        for (Component c : components) {
            int compWidth = getComponentWidth(c);
            int compX = contentX + (width - 16 - compWidth) / 2;

            c.mouseReleased(mouseX - compX, mouseY - yOffset, button);
            yOffset += c.getHeight() + 6;
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            x = (int) mouseX - offsetX;
            y = (int) mouseY - offsetY;
        } else {
            int startY = y + 35;
            int contentX = x + 8;
            int yOffset = startY;

            for (Component c : components) {
                int compWidth = getComponentWidth(c);
                int compX = contentX + (width - 16 - compWidth) / 2;

                c.mouseDragged(mouseX - compX, mouseY - yOffset, button, dx, dy);
                yOffset += c.getHeight() + 6;
            }
        }
    }
}