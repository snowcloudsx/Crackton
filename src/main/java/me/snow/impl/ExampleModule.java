// ===== FILE 3: ExampleModule.java =====
package me.snow.impl;

import me.snow.config.CracktonConfig;

/**
 * Example module implementation showing how to create modules
 */
public class ExampleModule extends Module {

    private int tickCounter = 0;
    private static final int EXECUTION_INTERVAL = 100; // Execute every 100 ticks (5 seconds at 20 TPS)

    public ExampleModule() {
        super("Example", "An example module", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        debugLog("Example module enabled with intensity: " + CracktonConfig.intensity);
        tickCounter = 0;
        notifyUser("Example module is now active!");
    }

    @Override
    public void onDisable() {
        debugLog("Example module disabled");
        notifyUser("Example module deactivated");
    }

    @Override
    public void onTick() {
        tickCounter++;

        // Execute main logic every EXECUTION_INTERVAL ticks
        if (tickCounter >= EXECUTION_INTERVAL) {
            executeMainLogic();
            tickCounter = 0;
        }

        // You can also add continuous per-tick logic here
        // For example: check for conditions, update states, etc.
    }



    @Override
    public void onDebugInfo() {
        super.onDebugInfo(); // Call parent debug info
        if (CracktonConfig.enableDebugMode) {
            debugLog("Tick Counter: " + tickCounter);
            debugLog("Execution Interval: " + EXECUTION_INTERVAL);
        }
    }

    /**
     * Main module logic - called every EXECUTION_INTERVAL ticks
     */
    private void executeMainLogic() {
        // Example: Process some value using config
        int processedValue = CracktonConfig.intensity * 2;

        debugLog("Executing main logic with processed value: " + processedValue);

        // Example: Do something based on the processed value
        if (processedValue > 10) {
            notifyUser("High intensity detected: " + processedValue);
        }

        // Add your actual module functionality here
        // This could be anything: automation, calculations, monitoring, etc.
    }

    /**
     * Example of a custom method that could be called from keybindings or other modules
     */
    public void performSpecialAction() {
        if (!isEnabled()) {
            notifyUser("Example module is not enabled!");
            return;
        }

        debugLog("Performing special action");
        notifyUser("Special action executed!");

        // Add your special action logic here
    }

    /**
     * Example getter for module-specific data
     */
    public int getCurrentTickCounter() {
        return tickCounter;
    }
}

// ===== USAGE INSTRUCTIONS =====
/*
To use this module system:

1. Create new modules by extending the Module class (like ExampleModule)
2. Register your modules in ModuleManager.init():
   ModuleManager.registerModule(new ExampleModule());
   ModuleManager.registerModule(new YourCustomModule());

3. In your main client tick loop, call:
   ModuleManager.onClientTick();

4. To control modules:
   ModuleManager.enableModule("example");
   ModuleManager.toggleModule("example");
   ModuleManager.disableModule("example");

5. For keybindings, you can create methods like:
   public static void toggleExampleModule() {
       ModuleManager.toggleModule("example");
   }

6. For cleanup (when shutting down):
   ModuleManager.shutdown();
*/