package me.snow.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class Checkbox extends Component {
    private final String text;
    private boolean checked;
    private final CheckboxCallback callback;

    public interface CheckboxCallback {
        void onToggle(boolean checked);
    }

    public Checkbox(String text, boolean defaultChecked, CheckboxCallback callback) {
        this.text = text;
        this.checked = defaultChecked;
        this.callback = callback;
    }

    @Override
    public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        boolean hovered = mouseX >= x && mouseX <= x + 150 && mouseY >= y && mouseY <= y + getHeight();

        // Checkbox box
        int boxSize = 12;
        int boxY = y + (getHeight() - boxSize) / 2;

        int boxColor = hovered ? 0xFF2A2A2A : 0xFF1E1E1E;
        ctx.fill(x, boxY, x + boxSize, boxY + boxSize, boxColor);
        ctx.drawBorder(x, boxY, boxSize, boxSize, 0xFF555555);

        // Checkmark
        if (checked) {
            // Draw a simple checkmark
            ctx.fill(x + 2, boxY + 5, x + 5, boxY + 8, 0xFF00FF00);
            ctx.fill(x + 4, boxY + 7, x + 10, boxY + 4, 0xFF00FF00);
            ctx.fill(x + 4, boxY + 8, x + 10, boxY + 5, 0xFF00FF00);
        }

        // Text
        int textX = x + boxSize + 5;
        int textY = y + (getHeight() - 8) / 2;
        int textColor = checked ? 0xFF00FF00 : 0xFFFFFFFF;
        ctx.drawText(MinecraftClient.getInstance().textRenderer, text, textX, textY, textColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= 0 && mouseX <= 150 && mouseY >= 0 && mouseY <= getHeight()) {
            checked = !checked;
            if (callback != null) {
                callback.onToggle(checked);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}