package me.snow.gui.widgets;

import me.snow.impl.Module;
import me.snow.impl.ModuleManager;
import me.snow.impl.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class ModuleToggle extends Component {
    // Constants for styling
    private static final int WIDTH = 164;
    private static final int TOGGLE_HEIGHT = 24;
    private static final int SETTING_INDENT = 8;
    private static final int SETTING_WIDTH = WIDTH - (SETTING_INDENT * 2);
    private static final int ANIMATION_SPEED = 8;

    // Color constants
    private static final int BG_COLOR = 0xF0101010;
    private static final int BG_HOVER_COLOR = 0xF0151515;
    private static final int BORDER_COLOR = 0x20FFFFFF;
    private static final int BORDER_HOVER_COLOR = 0x40FFFFFF;
    private static final int ENABLED_COLOR = 0xFF00DD00;
    private static final int DISABLED_COLOR = 0xFF444444;
    private static final int TEXT_ENABLED_COLOR = 0xFF00CC00;
    private static final int TEXT_ENABLED_HOVER_COLOR = 0xFF00FF00;
    private static final int TEXT_DISABLED_COLOR = 0xFFAAAAAA;
    private static final int TEXT_DISABLED_HOVER_COLOR = 0xFFCCCCCC;
    private static final int HIGHLIGHT_COLOR = 0x10FFFFFF;
    private static final int HIGHLIGHT_HOVER_COLOR = 0x20FFFFFF;

    private final Module module;
    private static final Map<String, Boolean> submenuVisibility = new HashMap<>();
    private static final Map<String, Boolean> sliderDragging = new HashMap<>();
    private static final Map<String, Float> hoverAnimations = new HashMap<>();
    private static final Map<String, Long> lastUpdateTime = new HashMap<>();

    public ModuleToggle(Module module) {
        this.module = module;
        submenuVisibility.put(module.getName(), false);
        sliderDragging.put(module.getName(), false);
        hoverAnimations.put(module.getName(), 0f);
        lastUpdateTime.put(module.getName(), System.currentTimeMillis());
    }

    @Override
    public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        updateAnimations(mouseX, mouseY, x, y);
        renderMainToggle(ctx, x, y, mouseX, mouseY);

        if (submenuVisibility.get(module.getName())) {
            renderSettings(ctx, x + SETTING_INDENT, y + TOGGLE_HEIGHT, mouseX - x - SETTING_INDENT, mouseY - y - TOGGLE_HEIGHT, delta);
        }
    }

    private void updateAnimations(int mouseX, int mouseY, int x, int y) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime.getOrDefault(module.getName(), currentTime)) / 1000f;
        lastUpdateTime.put(module.getName(), currentTime);

        boolean hovered = isHovered(mouseX, mouseY, x, y, WIDTH, TOGGLE_HEIGHT);
        float currentAnim = hoverAnimations.getOrDefault(module.getName(), 0f);

        if (hovered && currentAnim < 1f) {
            hoverAnimations.put(module.getName(), Math.min(1f, currentAnim + deltaTime * ANIMATION_SPEED));
        } else if (!hovered && currentAnim > 0f) {
            hoverAnimations.put(module.getName(), Math.max(0f, currentAnim - deltaTime * ANIMATION_SPEED));
        }
    }

    private void renderMainToggle(DrawContext ctx, int x, int y, int mouseX, int mouseY) {
        float hoverAnim = hoverAnimations.getOrDefault(module.getName(), 0f);
        boolean enabled = module.isEnabled();

        // Background with smooth hover effect
        int bgColor = interpolateColor(BG_COLOR, BG_HOVER_COLOR, hoverAnim);
        ctx.fill(x, y, x + WIDTH, y + TOGGLE_HEIGHT, bgColor);

        // Status indicator (left edge)
        int statusColor = enabled ? ENABLED_COLOR : DISABLED_COLOR;
        ctx.fill(x, y, x + 2, y + TOGGLE_HEIGHT, statusColor);

        // Module name (centered)
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textColor = enabled ?
                interpolateColor(TEXT_ENABLED_COLOR, TEXT_ENABLED_HOVER_COLOR, hoverAnim) :
                interpolateColor(TEXT_DISABLED_COLOR, TEXT_DISABLED_HOVER_COLOR, hoverAnim);

        drawCenteredText(ctx, textRenderer, module.getName(), x, y + (TOGGLE_HEIGHT - 8) / 2, WIDTH, textColor);

        // Border with hover effect
        int borderColor = interpolateColor(BORDER_COLOR, BORDER_HOVER_COLOR, hoverAnim);
        drawBorder(ctx, x, y, WIDTH, TOGGLE_HEIGHT, borderColor);

        // Top highlight
        int highlightColor = interpolateColor(HIGHLIGHT_COLOR, HIGHLIGHT_HOVER_COLOR, hoverAnim);
        ctx.fill(x + 1, y + 1, x + WIDTH - 1, y + 2, highlightColor);

        // Right-click indicator (only visible on hover)
        if (hoverAnim > 0) {
            String indicator = "•••";
            int indWidth = textRenderer.getWidth(indicator);
            int indColor = interpolateColor(0x00555555, 0x60555555, hoverAnim);
            ctx.drawText(textRenderer, indicator, x + WIDTH - indWidth - 6, y + (TOGGLE_HEIGHT - 8) / 2, indColor, false);
        }
    }

    private void renderSettings(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        if (module.getSettings().isEmpty()) {
            renderDescription(ctx, x, y);
            return;
        }

        int currentY = y;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                renderNumberSetting(ctx, x, currentY, (NumberSetting) setting, mouseX, mouseY);
                currentY += 28;
            } else if (setting instanceof BooleanSetting) {
                renderBooleanSetting(ctx, x, currentY, (BooleanSetting) setting, mouseX, mouseY);
                currentY += 24;
            } else if (setting instanceof ModeSetting) {
                renderModeSetting(ctx, x, currentY, (ModeSetting) setting, mouseX, mouseY);
                currentY += 24;
            }
        }
    }

    private void renderDescription(DrawContext ctx, int x, int y) {
        // Description background with subtle border
        ctx.fill(x, y, x + SETTING_WIDTH, y + 28, 0xF0080808);
        drawBorder(ctx, x, y, SETTING_WIDTH, 28, 0x30FFFFFF);

        // Description text (centered and wrapped if needed)
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        String desc = module.getDescription();

        // Simple text wrapping if needed
        if (textRenderer.getWidth(desc) > SETTING_WIDTH - 8) {
            // Split into two lines if too long
            int spaceIndex = desc.lastIndexOf(' ', desc.length() / 2);
            if (spaceIndex > 0) {
                String line1 = desc.substring(0, spaceIndex);
                String line2 = desc.substring(spaceIndex + 1);
                drawCenteredText(ctx, textRenderer, line1, x, y + 6, SETTING_WIDTH, 0xFFBBBBBB);
                drawCenteredText(ctx, textRenderer, line2, x, y + 16, SETTING_WIDTH, 0xFFBBBBBB);
                return;
            }
        }

        drawCenteredText(ctx, textRenderer, desc, x, y + 10, SETTING_WIDTH, 0xFFBBBBBB);
    }

    private void renderNumberSetting(DrawContext ctx, int x, int y, NumberSetting setting, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, 0, 0, SETTING_WIDTH, 28);
        boolean dragging = sliderDragging.getOrDefault(getSliderKey(setting), false);

        // Background with hover effect
        int bgColor = hovered || dragging ? BG_HOVER_COLOR : BG_COLOR;
        ctx.fill(x, y, x + SETTING_WIDTH, y + 28, bgColor);
        drawBorder(ctx, x, y, SETTING_WIDTH, 28, hovered || dragging ? BORDER_HOVER_COLOR : BORDER_COLOR);

        // Setting name (centered at top)
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        drawCenteredText(ctx, textRenderer, setting.getName(), x, y + 3, SETTING_WIDTH, 0xFFCCCCCC);

        // Slider track
        int trackWidth = SETTING_WIDTH - 18;
        int trackX = x + (SETTING_WIDTH - trackWidth) / 2;
        int trackY = y + 18;
        ctx.fill(trackX, trackY, trackX + trackWidth, trackY + 4, 0xFF202020);

        // Slider progress
        double progress = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int fillWidth = (int) (trackWidth * progress);
        int fillColor = dragging ? 0xFF00FF00 : hovered ? 0xFF00CC00 : 0xFF00AA00;
        ctx.fill(trackX, trackY, trackX + fillWidth, trackY + 4, fillColor);

        // Slider handle
        int handleX = trackX + fillWidth - 3;
        boolean handleHovered = isHovered(mouseX, mouseY, handleX - x, trackY - y - 2, 6, 8);
        int handleColor = dragging ? 0xFF00FF00 : handleHovered ? 0xFF00DD00 : 0xFFCCCCCC;
        ctx.fill(handleX, trackY - 2, handleX + 6, trackY + 6, handleColor);

        // Value display (with precision based on range)
        double range = setting.getMax() - setting.getMin();
        String format = range < 1 ? "%.3f" : range < 10 ? "%.2f" : "%.1f";
        String valueText = String.format(format, setting.getValue());
        ctx.drawText(textRenderer, valueText, x + SETTING_WIDTH - textRenderer.getWidth(valueText) - 4, y + 20, 0xFF888888, false);
    }

    private void renderBooleanSetting(DrawContext ctx, int x, int y, BooleanSetting setting, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, 0, 0, SETTING_WIDTH, 24);
        boolean enabled = setting.isEnabled();

        // Background with hover effect
        int bgColor = hovered ? BG_HOVER_COLOR : BG_COLOR;
        ctx.fill(x, y, x + SETTING_WIDTH, y + 24, bgColor);
        drawBorder(ctx, x, y, SETTING_WIDTH, 24, hovered ? BORDER_HOVER_COLOR : BORDER_COLOR);

        // Modern toggle switch
        int switchWidth = 32;
        int switchHeight = 14;
        int switchX = x + 8;
        int switchY = y + 5;

        // Switch track with animation
        float switchAnim = enabled ? 1f : 0f;
        int trackOffColor = 0xFF333333;
        int trackOnColor = 0x8000AA00;
        int trackColor = interpolateColor(trackOffColor, trackOnColor, switchAnim);
        ctx.fill(switchX, switchY, switchX + switchWidth, switchY + switchHeight, trackColor);

        // Switch handle with smooth animation
        int handleSize = switchHeight - 2;
        float handlePos = switchAnim * (switchWidth - handleSize - 2);
        int handleX = switchX + 1 + (int)handlePos;
        int handleY = switchY + 1;
        int handleColor = enabled ? 0xFF00FF00 : 0xFF666666;
        ctx.fill(handleX, handleY, handleX + handleSize, handleY + handleSize, handleColor);

        // Setting name (centered in remaining space)
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        String text = setting.getName();
        int textX = switchX + switchWidth + 8;
        int textWidth = SETTING_WIDTH - (switchWidth + 16);
        int textColor = enabled ? 0xFF00DD00 : 0xFFCCCCCC;
        drawCenteredText(ctx, textRenderer, text, textX, y + 8, textWidth, textColor);
    }

    private void renderModeSetting(DrawContext ctx, int x, int y, ModeSetting setting, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, 0, 0, SETTING_WIDTH, 24);

        // Background with hover effect
        int bgColor = hovered ? BG_HOVER_COLOR : BG_COLOR;
        ctx.fill(x, y, x + SETTING_WIDTH, y + 24, bgColor);
        drawBorder(ctx, x, y, SETTING_WIDTH, 24, hovered ? BORDER_HOVER_COLOR : BORDER_COLOR);

        // Setting text with current value
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        String text = setting.getName() + ": " + setting.getValue();
        drawCenteredText(ctx, textRenderer, text, x, y + 8, SETTING_WIDTH, 0xFFCCCCCC);

        // Navigation arrows (only visible on hover)
        if (hovered) {
            // Left arrow with hover effect
            boolean leftHover = isHovered(mouseX, mouseY, 8, 8, 8, 8);
            ctx.drawText(textRenderer, "◀", x + 8, y + 8, leftHover ? 0xFFAAAAAA : 0xFF888888, false);

            // Right arrow with hover effect
            boolean rightHover = isHovered(mouseX, mouseY, SETTING_WIDTH - 16, 8, 8, 8);
            ctx.drawText(textRenderer, "▶", x + SETTING_WIDTH - 16, y + 8, rightHover ? 0xFFAAAAAA : 0xFF888888, false);
        }
    }

    // Helper methods
    private void drawBorder(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.fill(x, y, x + width, y + 1, color); // Top
        ctx.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        ctx.fill(x, y, x + 1, y + height, color); // Left
        ctx.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    private void drawCenteredText(DrawContext ctx, net.minecraft.client.font.TextRenderer renderer, String text, int x, int y, int width, int color) {
        int textWidth = renderer.getWidth(text);
        ctx.drawText(renderer, text, x + (width - textWidth) / 2, y, color, false);
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
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

    // Input handling methods remain the same as in original...
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int)mouseX, (int)mouseY, 0, 0, WIDTH, TOGGLE_HEIGHT)) {
            if (button == 0) {
                ModuleManager.toggleModule(module.getName());
                return true;
            } else if (button == 1) {
                submenuVisibility.put(module.getName(), !submenuVisibility.get(module.getName()));
                return true;
            }
        }

        if (submenuVisibility.get(module.getName()) && mouseY >= TOGGLE_HEIGHT) {
            return handleSettingClick(mouseX - SETTING_INDENT, mouseY - TOGGLE_HEIGHT, button);
        }

        return false;
    }

    private boolean handleSettingClick(double mouseX, double mouseY, int button) {
        int currentY = 0;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting && mouseY >= currentY && mouseY <= currentY + 28) {
                if (button == 0) {
                    NumberSetting numberSetting = (NumberSetting) setting;
                    int trackY = currentY + 18;
                    int trackWidth = SETTING_WIDTH - 18;
                    int trackX = (SETTING_WIDTH - trackWidth) / 2;

                    if (isHovered((int)mouseX, (int)mouseY, trackX, trackY - 2, trackWidth, 8)) {
                        sliderDragging.put(getSliderKey(numberSetting), true);
                        updateSliderValue(numberSetting, mouseX - trackX, trackWidth);
                        return true;
                    }
                }
            } else if (setting instanceof BooleanSetting && mouseY >= currentY && mouseY <= currentY + 24) {
                if (button == 0) {
                    ((BooleanSetting) setting).toggle();
                    return true;
                }
            } else if (setting instanceof ModeSetting && mouseY >= currentY && mouseY <= currentY + 24) {
                ModeSetting modeSetting = (ModeSetting) setting;
                if (button == 0 && mouseX > SETTING_WIDTH - 16) {
                    modeSetting.nextMode();
                } else if (button == 1 || (button == 0 && mouseX < 16)) {
                    modeSetting.previousMode();
                }
                return true;
            }

            currentY += (setting instanceof NumberSetting) ? 28 : 24;
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            sliderDragging.keySet().removeIf(key -> key.startsWith(module.getName() + "_"));
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button == 0 && submenuVisibility.get(module.getName()) && mouseY >= TOGGLE_HEIGHT) {
            handleSliderDrag(mouseX - SETTING_INDENT, mouseY - TOGGLE_HEIGHT);
        }
    }

    private void handleSliderDrag(double mouseX, double mouseY) {
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                NumberSetting numberSetting = (NumberSetting) setting;
                if (sliderDragging.getOrDefault(getSliderKey(numberSetting), false)) {
                    int trackWidth = SETTING_WIDTH - 18;
                    int trackX = (SETTING_WIDTH - trackWidth) / 2;
                    updateSliderValue(numberSetting, mouseX - trackX, trackWidth);
                    return;
                }
            }
        }
    }

    private void updateSliderValue(NumberSetting setting, double relativeMouseX, int trackWidth) {
        double progress = MathHelper.clamp(relativeMouseX / trackWidth, 0, 1);
        double newValue = setting.getMin() + (setting.getMax() - setting.getMin()) * progress;
        setting.setValue(newValue);
    }

    private String getSliderKey(NumberSetting setting) {
        return module.getName() + "_" + setting.getName();
    }

    @Override
    public int getHeight() {
        if (!submenuVisibility.get(module.getName())) {
            return TOGGLE_HEIGHT;
        }

        int height = TOGGLE_HEIGHT;
        if (module.getSettings().isEmpty()) {
            return height + 28; // Description height
        }

        for (Setting<?> setting : module.getSettings()) {
            height += (setting instanceof NumberSetting) ? 28 : 24;
        }
        return height;
    }//i like this one more (the oldone)

    public int getWidth() {
        return WIDTH;
    }
}