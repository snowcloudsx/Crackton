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
                "key.crackton.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.crackton"
        ));

        QUICK_TOGGLE_FEATURE1 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crackton.toggle_feature1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M, // No default key
                "category.crackton"
        ));

        QUICK_TOGGLE_FEATURE2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crackton.toggle_feature2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // No default key
                "category.crackton"
        ));

        QUICK_TOGGLE_DEBUG = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crackton.toggle_debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // No default key
                "category.crackton"
        ));
    }
}