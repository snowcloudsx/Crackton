package me.snow.gui;

import me.snow.gui.widgets.Component;
import me.snow.gui.widgets.ModuleToggle;
import me.snow.gui.widgets.Panel;
import me.snow.impl.Category;
import me.snow.impl.Module;
import me.snow.impl.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GUI extends Screen {
    private final Screen parent;
    private final List<Panel> panels = new ArrayList<>();

    public GUI(Screen parent) {
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
                toggles.add(new ModuleToggle(module));
            }

            if (!toggles.isEmpty()) {
                panels.add(new Panel(ModuleManager.GetNameOfCategory(category), x, y, toggles));
                x += 200;
            }
        }

        // Add example panel with all widget types
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
}