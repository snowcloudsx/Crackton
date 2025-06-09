package me.snow;

import me.snow.config.CracktonConfig;
import me.snow.impl.ExampleModule;
import me.snow.impl.ModuleManager;
import me.snow.gui.CracktonConfigScreen;
import me.snow.keybinds.KeyBindings;
import me.snow.notifications.NotificationManager;
import me.snow.utils.InventoryUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crackton implements ClientModInitializer {
	public static final String MOD_ID = "crackton";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {


		LOGGER.info("Crackton Initialized!");

		// Initialize configuration
		CracktonConfig.init();

		// Initialize features manager
		ModuleManager.init();

		// Register keybindings
		KeyBindings.register();

		// Register client tick event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Handle GUI keybinding

			ModuleManager.onClientTick();


			if (KeyBindings.OPEN_GUI.wasPressed()) {
				client.setScreen(new CracktonConfigScreen(client.currentScreen));
			}

			// Handle quick toggle keybindings
			if (KeyBindings.QUICK_TOGGLE_FEATURE1.wasPressed()) {
				// Basic notifications

				ModuleManager.enableModule("Hover Totem");
			}

			if (KeyBindings.QUICK_TOGGLE_FEATURE2.wasPressed()) {
				//ModuleManager.quickToggleFeature2();
			}

			if (KeyBindings.QUICK_TOGGLE_DEBUG.wasPressed()) {
			//	ModuleManager.quickToggleDebug();
			}

			// Process per-tick features
			//ModuleManager.onClientTick();
		});
	}
}