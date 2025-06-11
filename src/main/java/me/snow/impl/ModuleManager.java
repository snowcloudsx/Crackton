package me.snow.impl;

import me.snow.Crackton;
import me.snow.config.CracktonConfig;
import me.snow.impl.Render.AntiTrap;
import me.snow.impl.Render.PlayerESP;
import me.snow.impl.combat.HoverTotem;
import me.snow.impl.combat.TriggerBot;
import me.snow.impl.misc.BoneDropper;
import me.snow.impl.player.Fastplace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all modules with proper lifecycle and ordering
 */
public class ModuleManager {

    private static final Map<String, Module> modules = new ConcurrentHashMap<>();
    private static final List<Module> enabledModules = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        // Register your modules here
        // Example: registerModule(new ExampleModule());

        // Sort by priority for consistent execution order

        // Load initial config

        ModuleManager.registerModule(new TriggerBot());
        ModuleManager.registerModule(new HoverTotem());
        ModuleManager.registerModule(new Fastplace());
        ModuleManager.registerModule(new PlayerESP());
        ModuleManager.registerModule(new AntiTrap());
        ModuleManager.registerModule(new BoneDropper());


        initialized = true;
        Crackton.LOGGER.info("Module Manager initialized with " + modules.size() + " modules");
    }

    /**
     * Register a new module
     */
    public static void registerModule(Module module) {
        modules.put(module.getName().toLowerCase(), module);

        // Add module to its category
        if (module.getCategory() != null) {
            module.getCategory().addModule(module);
        }

        Crackton.LOGGER.info("Registered module: " + module.getName());
    }


    /**
     * Get module by name
     */
    public static Module getModule(String name) {
        return modules.get(name.toLowerCase());
    }

    /**
     * Get all modules
     */
    public static Collection<Module> getAllModules() {
        return modules.values();
    }

    /**
     * Get enabled modules only
     */
    public static List<Module> getEnabledModules() {
        return new ArrayList<>(enabledModules);
    }

    /**
     * Enable a module by name
     */
    public static boolean enableModule(String name) {
        Module module = getModule(name);
        if (module != null) {
            module.setEnabled(true);
            updateEnabledModulesList();
            return true;
        }
        return false;
    }

    public static String GetNameOfCategory(Category category) {
        return category.name();
    }


    /**
     * Disable a module by name
     */
    public static boolean disableModule(String name) {
        Module module = getModule(name);
        if (module != null) {
            module.setEnabled(false);
            updateEnabledModulesList();
            return true;
        }
        return false;
    }

    /**
     * Toggle a module by name
     */
    public static boolean toggleModule(String name) {
        Module module = getModule(name);
        if (module != null) {
            module.toggle(module);
            updateEnabledModulesList();
            return true;
        }
        return false;
    }

    /**
     * Called every client tick
     */
    public static void onClientTick() {
        for (Module module : enabledModules) {
            try {
                module.onTick();
            } catch (Exception e) {
                Crackton.LOGGER.error("Error in module " + module.getName() + " tick: " + e.getMessage());
            }
        }

    }

    /**
     * Save all module configurations
     */

    /**
     * Display debug information for all modules
     */
    public static void logDebugInfo() {
        if (!CracktonConfig.enableDebugMode) return;

        Crackton.LOGGER.info("=== Module Manager Debug Info ===");
        Crackton.LOGGER.info("Total modules: " + modules.size());
        Crackton.LOGGER.info("Enabled modules: " + enabledModules.size());

        for (Module module : modules.values()) {
            module.onDebugInfo();
        }
        Crackton.LOGGER.info("================================");
    }

    /**
     * Shutdown all modules gracefully
     */
    public static void shutdown() {
        for (Module module : enabledModules) {
            try {
                module.setEnabled(false);
            } catch (Exception e) {
                Crackton.LOGGER.error("Error shutting down " + module.getName() + ": " + e.getMessage());
            }
        }

        modules.clear();
        enabledModules.clear();
        initialized = false;

        Crackton.LOGGER.info("Module Manager shutdown complete");
    }

    // Private helper methods
    private static void updateEnabledModulesList() {
        enabledModules.clear();
        for (Module module : modules.values()) {
            if (module.isEnabled()) {
                enabledModules.add(module);
            }
        }
    }





    private static void loadModuleStates() {
        // Load module states from config
        // This is a placeholder - implement based on your config system
        // Example usage:
        // if (CracktonConfig.enableFeature1) {
        //     enableModule("example");
        // }
    }
}