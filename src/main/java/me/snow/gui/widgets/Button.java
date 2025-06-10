package me.snow.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class Button extends Component {
    private final String text;
    private final int width;
    private final ButtonCallback callback;
    private boolean pressed = false;

    public interface ButtonCallback {
        void onPressed();
    }

    public Button(String text, int width, ButtonCallback callback) {
        this.text = text;
        this.width = width;
        this.callback = callback;
    }

    public Button(String text, ButtonCallback callback) {
        this(text, 150, callback);
    }

    @Override
    public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getHeight();

        int bgColor;
        if (pressed) {
            bgColor = 0xFF1A4A1A;
        } else if (hovered) {
            bgColor = 0xFF2A2A2A;
        } else {
            bgColor = 0xFF1E1E1E;
        }

        // Background
        ctx.fill(x, y, x + width, y + getHeight(), bgColor);

        // Border
        int borderColor = hovered ? 0xFF555555 : 0xFF444444;
        ctx.drawBorder(x, y, width, getHeight(), borderColor);

        // Text
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (getHeight() - 8) / 2;

        int textColor = pressed ? 0xFF00FF00 : 0xFFFFFFFF;
        ctx.drawText(MinecraftClient.getInstance().textRenderer, text, textX, textY, textColor, false);

        // Reset pressed state after a short time
        if (pressed) {
            // This creates a brief visual feedback
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    pressed = false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY <= getHeight()) {
            pressed = true;
            if (callback != null) {
                callback.onPressed();
            }
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return 20;
    }
}