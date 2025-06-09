package me.snow.config;

import me.snow.Crackton;

public class CracktonConfig {

    public static boolean enableDebugMode = false;
    public static boolean enableAutoSave = true;
    public static boolean enableNotifications = true;

    // Numeric settings
    public static int intensity = 50;
    public static float multiplier = 1.0f;

    // Future settings can be added here easily
    public static boolean enableAdvancedMode = false;
    public static int maxConnections = 10;
    public static String serverUrl = "localhost:8080";

    public static void init() {
        // Load configuration from file if needed
        // For now, using default values
        Crackton.LOGGER.info("Configuration initialized with default values");
    }

    public static void save() {
        // Save configuration to file
        // Implementation can be added later
        Crackton.LOGGER.info("Configuration saved");
    }

    public static void load() {
        // Load configuration from file
        // Implementation can be added later
        Crackton.LOGGER.info("Configuration loaded");
    }

    public static void reset() {

        enableDebugMode = false;
        enableAutoSave = true;
        enableNotifications = true;
        intensity = 50;
        multiplier = 1.0f;
        enableAdvancedMode = false;
        maxConnections = 10;
        serverUrl = "localhost:8080";

        Crackton.LOGGER.info("Configuration reset to defaults");
    }
}