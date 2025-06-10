package me.snow.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class Slider extends Component {
    private final String name;
    private final double min, max;
    private double value;
    private boolean dragging = false;
    private final int width = 150;
    private final SliderCallback callback;

    public interface SliderCallback {
        void onValueChanged(double value);
    }

    public Slider(String name, double min, double max, double defaultValue, SliderCallback callback) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = MathHelper.clamp(defaultValue, min, max);
        this.callback = callback;
    }

    @Override
    public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        // Background
        ctx.fill(x, y, x + width, y + getHeight(), 0xFF1E1E1E);

        // Slider track
        int trackY = y + 15;
        ctx.fill(x + 5, trackY, x + width - 5, trackY + 4, 0xFF333333);

        // Slider fill (progress)
        double progress = (value - min) / (max - min);
        int fillWidth = (int) ((width - 10) * progress);
        ctx.fill(x + 5, trackY, x + 5 + fillWidth, trackY + 4, 0xFF00AA00);

        // Slider handle
        int handleX = x + 5 + fillWidth - 3;
        boolean handleHovered = mouseX >= handleX && mouseX <= handleX + 6 && mouseY >= trackY - 2 && mouseY <= trackY + 6;
        int handleColor = handleHovered || dragging ? 0xFF00FF00 : 0xFFAAAAAA;
        ctx.fill(handleX, trackY - 2, handleX + 6, trackY + 6, handleColor);

        // Label and value text
        String displayText = name + ": " + String.format("%.2f", value);
        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, x + 5, y + 3, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY <= getHeight()) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            updateValue(mouseX);
        }
    }

    private void updateValue(double mouseX) {
        double progress = MathHelper.clamp((mouseX - 5) / (width - 10), 0.0, 1.0);
        double newValue = min + (max - min) * progress;

        // Round to 2 decimal places
        newValue = Math.round(newValue * 100.0) / 100.0;

        if (newValue != value) {
            value = newValue;
            if (callback != null) {
                callback.onValueChanged(value);
            }
        }
    }

    @Override
    public int getHeight() {
        return 25;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = MathHelper.clamp(value, min, max);
    }
}