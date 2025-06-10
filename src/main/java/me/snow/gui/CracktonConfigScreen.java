// Enhanced CracktonConfigScreen with clean design, proper toggles, and right-click submenus
package me.snow.gui;

import me.snow.config.CracktonConfig;
import me.snow.impl.Module;
import me.snow.impl.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import me.snow.impl.Category;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CracktonConfigScreen extends Screen {
    private final Screen parent;
    private final List<Panel> panels = new ArrayList<>();
    private final Map<String, Boolean> submenuVisibility = new HashMap<>();

    public CracktonConfigScreen(Screen parent) {
        super(Text.literal("Crackton"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panels.clear(); // Clear existing panels first

        int x = 30;
        int y = 40;

        for (Category category : Category.values()) {
            List<Component> toggles = new ArrayList<>();

            for (Module module : category.getModules()) {
                toggles.add(new ModuleToggle(module)); // uses updated constructor
            }

            if (!toggles.isEmpty()) {
                panels.add(new Panel(ModuleManager.GetNameOfCategory(category), x, y, toggles));
                x += 200;
            }
        }

    }




    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        for (Panel panel : panels) panel.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        for (Panel panel : panels) panel.mouseDragged(mouseX, mouseY, button, dx, dy);
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private class Panel {
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
            ctx.drawText(textRenderer, title, x + 6, y + 6, 0xFFFFFFAA, false);

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
        }

        public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            if (dragging) {
                x = (int) mouseX - offsetX;
                y = (int) mouseY - offsetY;
            }
        }
    }

    private abstract class Component {
        public abstract void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta);
        public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
        public int getHeight() { return 20; }
    }

    private class ModuleToggle extends Component {
        private final Module module;

        public ModuleToggle(Module module) {
            this.module = module;
            submenuVisibility.put(module.getName(), false);
        }

        @Override
        public void render(DrawContext ctx, int x, int y, int mouseX, int mouseY, float delta) {
            boolean hovered = mouseX >= x && mouseX <= x + 160 && mouseY >= y && mouseY <= y + 16;
            int bgColor = hovered ? 0xFF2A2A2A : 0xFF1E1E1E;
            ctx.fill(x, y, x + 160, y + 16, bgColor);

            ctx.drawText(textRenderer, module.getName(), x + 5, y + 4,
                    module.isEnabled() ? 0xFF00FF00 : 0xFFFF5555, false);

            if (submenuVisibility.get(module.getName())) {
                ctx.fill(x + 10, y + 20, x + 150, y + 44, 0xFF121212);
                ctx.drawText(textRenderer, module.getDescription(), x + 12, y + 26, 0xFFFFFFAA, false);
            }
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
            return false;
        }

        @Override
        public int getHeight() {
            return submenuVisibility.get(module.getName()) ? 44 : 20;
        }
    }

}