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

			/*
			if (client.world != null && client.player != null && fakePlayer == null) {
				GameProfile profile = new GameProfile(UUID.randomUUID(), "Target");
				fakePlayer = new OtherClientPlayerEntity(client.world, profile) {
					public boolean damage(DamageSource source, float amount) {
						this.timeUntilRegen = 10;
						setHealth(getHealth() - amount);
						playHurtSound(source);
						return true;
					}
					public boolean canHit() { return true; }
					public boolean canTakeDamage() { return true; }
					public boolean isInvulnerableTo(DamageSource source) { return false; }
					public boolean isPushable() { return true; }
					public boolean canBeHitByProjectile() { return true; }
					public boolean isAttackable() { return true; }
					public boolean shouldRender() { return true; }
				};
				fakePlayer.setHealth(20f);
				fakePlayer.setPosition(client.player.getX() + 2, client.player.getY(), client.player.getZ());
				fakePlayer.setInvulnerable(false);
				fakePlayer.setInvisible(false);
				fakePlayer.calculateDimensions();
				fakePlayer.setBoundingBox(fakePlayer.getType().getDimensions().getBoxAt(fakePlayer.getPos()));
				client.world.addEntity(fakePlayer);
			}
			*/

			if (KeyBindings.OPEN_GUI.wasPressed()) {
				client.setScreen(new GUI(client.currentScreen));
			}

			if (KeyBindings.QUICK_TOGGLE_FEATURE1.wasPressed()) {
				ModuleManager.toggleModule("FastPlace");
			}
		});
	}
}
