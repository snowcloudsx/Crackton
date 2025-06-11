package me.snow;

import com.mojang.authlib.GameProfile;
import me.snow.config.CracktonConfig;
import me.snow.gui.hud.HudManager;
import me.snow.impl.ModuleManager;
import me.snow.gui.GUI;
import me.snow.keybinds.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Crackton implements ClientModInitializer {
	public static final String MOD_ID = "crackton";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	static OtherClientPlayerEntity fakePlayer = null;


	@Override
	public void onInitializeClient() {
		LOGGER.info("Crackton Initialized!");



		// Initialize components
		CracktonConfig.init();
		ModuleManager.init();
		KeyBindings.register();
		HudManager.init();

		// Register HUD rendering
		// Register HUD rendering
		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
			float tickDelta = tickCounter.getTickDelta(true); // NEW: Extract tickDelta from RenderTickCounter
			HudManager.getInstance().render(drawContext, tickDelta);
		});


		// Register client tick event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ModuleManager.onClientTick();




			if (KeyBindings.OPEN_GUI.wasPressed()) {
				client.setScreen(new GUI(client.currentScreen));
			}

			if (KeyBindings.QUICK_TOGGLE_FEATURE1.wasPressed()) {
				ModuleManager.toggleModule("FastPlace");
			}
		});
	}
}
