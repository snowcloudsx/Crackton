// ===== FILE 1: Module.java =====
package me.snow.impl;

import me.snow.Crackton;
import me.snow.config.CracktonConfig;
import me.snow.notifications.NotificationManager;
import me.snow.impl.Category;
/**
 * Abstract base class for all modules
 */
public abstract class Module {


    protected final String name;
    protected final String description;
    protected boolean enabled;
    protected Category cate;

    public Module(String name, String description, Category cate) {
        this.name = name;
        this.description = description;
        this.enabled = false;
        this.cate = cate;
    }

    // Abstract methods that each module must implement
    public abstract void onEnable();
    public abstract void onDisable();
    public abstract void onTick();

    // Optional overrideable methods

    public void onDebugInfo() {
        Crackton.LOGGER.info(name + ": " + (enabled ? "enabled" : "disabled"));
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public Category getCategory() { return  cate; }

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

    public void toggle() {
        setEnabled(!enabled);
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