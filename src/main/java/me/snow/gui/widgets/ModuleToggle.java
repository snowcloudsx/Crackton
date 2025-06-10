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
    private final Module module;
    private static final Map<String, Boolean> submenuVisibility = new HashMap<>();
    private static final Map<String, Boolean> sliderDragging = new HashMap<>();

    public ModuleToggle(Module module) {
        this.module = module;
        submenuVisibility.put(module.getName(), false);
        sliderDragging.put(module.getName(), false);
    }

    @Override
    public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        boolean hovered = mouseX >= x && mouseX <= x + 160 && mouseY >= y && mouseY <= y + 16;
        int bgColor = hovered ? 0xFF2A2A2A : 0xFF1E1E1E;
        ctx.fill(x, y, x + 160, y + 16, bgColor);

        ctx.drawText(MinecraftClient.getInstance().textRenderer, module.getName(), x + 5, y + 4,
                module.isEnabled() ? 0xFF00FF00 : 0xFFFF5555, false);

        if (submenuVisibility.get(module.getName())) {
            renderSettings(ctx, x, y + 20, mouseX, mouseY, delta);
        }
    }

    private void renderSettings(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
        if (module.getSettings().isEmpty()) {
            // Show description if no settings
            ctx.fill(x + 10, y, x + 150, y + 24, 0xFF121212);
            ctx.drawText(MinecraftClient.getInstance().textRenderer, module.getDescription(), x + 12, y + 8, 0xFFFFFFAA, false);
            return;
        }

        int currentY = y;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                NumberSetting numberSetting = (NumberSetting) setting;
                renderNumberSetting(ctx, x + 10, currentY, numberSetting, mouseX - x - 10, mouseY - currentY);
                currentY += 25;
            } else if (setting instanceof BooleanSetting) {
                BooleanSetting boolSetting = (BooleanSetting) setting;
                renderBooleanSetting(ctx, x + 10, currentY, boolSetting, mouseX - x - 10, mouseY - currentY);
                currentY += 20;
            } else if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting) setting;
                renderModeSetting(ctx, x + 10, currentY, modeSetting, mouseX - x - 10, mouseY - currentY);
                currentY += 20;
            }
        }
    }

    private void renderNumberSetting(DrawContext ctx, int x, int y, NumberSetting setting, int mouseX, int mouseY) {
        // Background
        ctx.fill(x, y, x + 140, y + 25, 0xFF1A1A1A);

        // Slider track
        int trackY = y + 15;
        ctx.fill(x + 5, trackY, x + 135, trackY + 3, 0xFF333333);

        // Slider fill
        double progress = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int fillWidth = (int) (130 * progress);
        ctx.fill(x + 5, trackY, x + 5 + fillWidth, trackY + 3, 0xFF00AA00);

        // Slider handle
        int handleX = x + 5 + fillWidth - 2;
        boolean handleHovered = mouseX >= handleX && mouseX <= handleX + 4 && mouseY >= trackY - 1 && mouseY <= trackY + 4;
        int handleColor = handleHovered || sliderDragging.getOrDefault(getSliderKey(setting), false) ? 0xFF00FF00 : 0xFFAAAAAA;
        ctx.fill(handleX, trackY - 1, handleX + 4, trackY + 4, handleColor);

        // Text
        String text = setting.getName() + ": " + String.format("%.2f", setting.getValue());
        ctx.drawText(MinecraftClient.getInstance().textRenderer, text, x + 5, y + 3, 0xFFFFFFFF, false);
    }

    private void renderBooleanSetting(DrawContext ctx, int x, int y, BooleanSetting setting, int mouseX, int mouseY) {
        // Background
        boolean hovered = mouseX >= 0 && mouseX <= 140 && mouseY >= 0 && mouseY <= 20;
        ctx.fill(x, y, x + 140, y + 20, hovered ? 0xFF2A2A2A : 0xFF1A1A1A);

        // Checkbox
        int boxSize = 10;
        int boxX = x + 5;
        int boxY = y + 5;
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF333333);

        if (setting.isEnabled()) {
            ctx.fill(boxX + 2, boxY + 2, boxX + boxSize - 2, boxY + boxSize - 2, 0xFF00FF00);
        }

        // Text
        int textColor = setting.isEnabled() ? 0xFF00FF00 : 0xFFFFFFFF;
        ctx.drawText(MinecraftClient.getInstance().textRenderer, setting.getName(), boxX + boxSize + 5, y + 6, textColor, false);
    }

    private void renderModeSetting(DrawContext ctx, int x, int y, ModeSetting setting, int mouseX, int mouseY) {
        // Background
        boolean hovered = mouseX >= 0 && mouseX <= 140 && mouseY >= 0 && mouseY <= 20;
        ctx.fill(x, y, x + 140, y + 20, hovered ? 0xFF2A2A2A : 0xFF1A1A1A);

        // Text
        String text = setting.getName() + ": " + setting.getValue();
        ctx.drawText(MinecraftClient.getInstance().textRenderer, text, x + 5, y + 6, 0xFFFFFFFF, false);

        // Arrow indicator
        ctx.drawText(MinecraftClient.getInstance().textRenderer, "◀ ▶", x + 120, y + 6, 0xFFAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= 0 && mouseX <= 160 && mouseY >= 0 && mouseY <= 16) {
            if (button == 0) {
                ModuleManager.toggleModule(module.getName());
                return true;
            } else if (button == 1) {
                submenuVisibility.put(module.getName(), !submenuVisibility.get(module.getName()));
                return true;
            }
        }

        // Handle setting clicks when submenu is open
        if (submenuVisibility.get(module.getName()) && mouseY >= 20) {
            return handleSettingClick(mouseX - 10, mouseY - 20, button);
        }

        return false;
    }

    private boolean handleSettingClick(double mouseX, double mouseY, int button) {
        int currentY = 0;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting && mouseY >= currentY && mouseY <= currentY + 25) {
                if (button == 0) {
                    NumberSetting numberSetting = (NumberSetting) setting;
                    // Check if click is within slider area
                    if (mouseX >= 5 && mouseX <= 135 && mouseY >= currentY + 14 && mouseY <= currentY + 18) {
                        sliderDragging.put(getSliderKey(numberSetting), true);
                        updateSliderValue(numberSetting, mouseX);
                        return true;
                    }
                }
            } else if (setting instanceof BooleanSetting && mouseY >= currentY && mouseY <= currentY + 20) {
                if (button == 0) {
                    ((BooleanSetting) setting).toggle();
                    return true;
                }
            } else if (setting instanceof ModeSetting && mouseY >= currentY && mouseY <= currentY + 20) {
                ModeSetting modeSetting = (ModeSetting) setting;
                if (button == 0) {
                    modeSetting.nextMode();
                } else if (button == 1) {
                    modeSetting.previousMode();
                }
                return true;
            }

            // Update Y position based on setting type
            if (setting instanceof NumberSetting) {
                currentY += 25;
            } else {
                currentY += 20;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Stop all slider dragging for this module
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof NumberSetting) {
                    sliderDragging.put(getSliderKey((NumberSetting) setting), false);
                }
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button == 0 && submenuVisibility.get(module.getName()) && mouseY >= 20) {
            handleSliderDrag(mouseX - 10, mouseY - 20);
        }
    }

    private void handleSliderDrag(double mouseX, double mouseY) {
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                NumberSetting numberSetting = (NumberSetting) setting;
                String sliderKey = getSliderKey(numberSetting);

                if (sliderDragging.getOrDefault(sliderKey, false)) {
                    updateSliderValue(numberSetting, mouseX);
                    return;
                }
            }
        }
    }

    private void updateSliderValue(NumberSetting setting, double mouseX) {
        // Clamp mouseX to slider bounds
        double clampedMouseX = Math.max(5, Math.min(135, mouseX));

        // Calculate progress based on mouse position
        double progress = (clampedMouseX - 5) / 130.0;
        double newValue = setting.getMin() + (setting.getMax() - setting.getMin()) * progress;

        setting.setValue(newValue);
    }

    private String getSliderKey(NumberSetting setting) {
        return module.getName() + "_" + setting.getName();
    }

    @Override
    public int getHeight() {
        if (!submenuVisibility.get(module.getName())) {
            return 20;
        }

        if (module.getSettings().isEmpty()) {
            return 44; // Space for description
        }

        int height = 20; // Base toggle height
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                height += 25;
            } else {
                height += 20;
            }
        }
        return height;
    }
}