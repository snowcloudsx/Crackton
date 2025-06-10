package me.snow.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding OPEN_GUI;
    public static KeyBinding QUICK_TOGGLE_FEATURE1;
    public static KeyBinding QUICK_TOGGLE_FEATURE2;
    public static KeyBinding QUICK_TOGGLE_DEBUG;

    public static void register() {
        OPEN_GUI = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "GUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "crackton"
        ));

        QUICK_TOGGLE_FEATURE1 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Current Debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M, // No default key
                "crackton"
        ));


    }
}