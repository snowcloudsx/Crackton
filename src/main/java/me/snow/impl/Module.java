package me.snow.impl;

import me.snow.Crackton;
import me.snow.config.CracktonConfig;
import me.snow.notifications.NotificationManager;
import me.snow.impl.Category;
import me.snow.impl.settings.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all modules
 */
public abstract class Module {

    protected final String name;
    protected final String description;
    protected boolean enabled;
    protected Category cate;
    protected final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category cate) {
        this.name = name;
        this.description = description;
        this.enabled = false;
        this.cate = cate;
        initSettings(); // Initialize settings when module is created
    }

    // Abstract methods that each module must implement
    public abstract void onEnable();
    public abstract void onDisable();
    public abstract void onTick();

    // Override this to add settings to your module
    protected void initSettings() {
        // Override in subclasses to add settings
    }

    // Setting management methods
    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    protected NumberSetting addNumberSetting(String name, String description, double defaultValue, double min, double max) {
        NumberSetting setting = new NumberSetting(name, description, defaultValue, min, max);
        addSetting(setting);
        return setting;
    }

    protected BooleanSetting addBooleanSetting(String name, String description, boolean defaultValue) {
        BooleanSetting setting = new BooleanSetting(name, description, defaultValue);
        addSetting(setting);
        return setting;
    }

    protected ModeSetting addModeSetting(String name, String description, String defaultValue, String... modes) {
        ModeSetting setting = new ModeSetting(name, description, defaultValue, modes);
        addSetting(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() {
        return new ArrayList<>(settings);
    }

    @SuppressWarnings("unchecked")
    public <T extends Setting<?>> T getSetting(String name) {
        return (T) settings.stream()
                .filter(setting -> setting.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // Optional overrideable methods
    public void onDebugInfo() {
        Crackton.LOGGER.info(name + ": " + (enabled ? "enabled" : "disabled"));
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public Category getCategory() { return cate; }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }

            if (CracktonConfig.enableNotifications) {
                notifyUser(name + " " + (enabled ? "enabled" : "disabled"));
            }
        }
    }

    public void toggle(Module mod) {
        mod.setEnabled(!mod.enabled);
    }

    protected void notifyUser(String message) {
        NotificationManager.sendChatMessage(message);
    }

    protected void debugLog(String message) {
        if (CracktonConfig.enableDebugMode) {
            Crackton.LOGGER.info("[" + name + " Debug] " + message);
        }
    }
}