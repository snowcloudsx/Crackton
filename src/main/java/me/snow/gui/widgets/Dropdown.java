package me.snow.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class Dropdown extends Component {
    private final String name;
    private final List<String> options;
    private int selectedIndex = 0;
    private boolean expanded = false;
    private final int width = 150;
    private final DropdownCallback callback;

    public interface DropdownCallback {
        void onSelectionChanged(String selected, int index);
    }

    public Dropdown(String name, List<String> options, int defaultIndex, DropdownCallback callback) {
        this.name = name;
        this.options = options;
        this.selectedIndex = Math.max(0, Math.min(defaultIndex, options.size() - 1));
        this.callback = callback;
    }

    @Override
    public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        // Main dropdown box
        boolean mainHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 18;
        int bgColor = mainHovered ? 0xFF2A2A2A : 0xFF1E1E1E;
        ctx.fill(x, y, x + width, y + 18, bgColor);

        // Border
        ctx.drawBorder(x, y, width, 18, 0xFF444444);

        // Label and selected value
        String displayText = name + ": " + (options.isEmpty() ? "None" : options.get(selectedIndex));
        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, x + 5, y + 5, 0xFFFFFFFF, false);

        // Dropdown arrow
        String arrow = expanded ? "▲" : "▼";
        int arrowX = x + width - 15;
        ctx.drawText(MinecraftClient.getInstance().textRenderer, arrow, arrowX, y + 5, 0xFFAAAAAA, false);

        // Expanded options
        if (expanded && !options.isEmpty()) {
            int optionY = y + 20;
            for (int i = 0; i < options.size(); i++) {
                boolean optionHovered = mouseX >= x && mouseX <= x + width &&
                        mouseY >= optionY && mouseY <= optionY + 16;

                int optionBg = optionHovered ? 0xFF333333 :
                        (i == selectedIndex ? 0xFF2A4A2A : 0xFF252525);

                ctx.fill(x, optionY, x + width, optionY + 16, optionBg);
                ctx.drawBorder(x, optionY, width, 16, 0xFF444444);

                int textColor = i == selectedIndex ? 0xFF00FF00 : 0xFFFFFFFF;
                ctx.drawText(MinecraftClient.getInstance().textRenderer, options.get(i),
                        x + 5, optionY + 4, textColor, false);

                optionY += 16;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= 0 && mouseX <= width) {
            // Click on main dropdown
            if (mouseY >= 0 && mouseY <= 18) {
                expanded = !expanded;
                return true;
            }

            // Click on expanded options
            if (expanded && !options.isEmpty()) {
                int optionIndex = (int) ((mouseY - 20) / 16);
                if (optionIndex >= 0 && optionIndex < options.size() && mouseY >= 20) {
                    selectedIndex = optionIndex;
                    expanded = false;

                    if (callback != null) {
                        callback.onSelectionChanged(options.get(selectedIndex), selectedIndex);
                    }
                    return true;
                }
            }
        }

        // Click outside to close
        if (expanded) {
            expanded = false;
            return true;
        }

        return false;
    }

    @Override
    public int getHeight() {
        return expanded ? 20 + (options.size() * 16) : 20;
    }

    public String getSelected() {
        return options.isEmpty() ? null : options.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelected(int index) {
        if (index >= 0 && index < options.size()) {
            selectedIndex = index;
        }
    }

    public void setSelected(String option) {
        int index = options.indexOf(option);
        if (index != -1) {
            selectedIndex = index;
        }
    }
}