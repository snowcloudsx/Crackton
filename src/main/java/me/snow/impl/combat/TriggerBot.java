package me.snow.impl.combat;

import it.unimi.dsi.fastutil.booleans.BooleanSet;
import me.snow.impl.Category;
import me.snow.impl.Module;
import me.snow.impl.settings.BooleanSetting;
import me.snow.impl.settings.NumberSetting;
import me.snow.mixin.HandledScreenAccessor;
import me.snow.utils.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TriggerBot extends Module {

    private NumberSetting EquipDelay;
    private NumberSetting DelayRandomization;
    private BooleanSetting RequireCrosshair;
    private static final int DELAY_TICKS = 10;
    private int delayCounter = 0;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public TriggerBot() {
        super("TriggerBot", "Automatically hits ", Category.COMBAT);
    }


    @Override
    public void onEnable() {
        delayCounter = 0;
    }

    @Override
    protected void initSettings() {
        // Default to 600ms (Minecraft sword cooldown), range 50-2000ms
        EquipDelay = addNumberSetting("Hit Delay", "Sets the Delay before You Hit (ms)", 600, 50, 2000);
        // Add randomization setting
        DelayRandomization = addNumberSetting("Delay Randomization", "Random delay variation (ms)", 100, 0, 300);
        // Add range check
        RequireCrosshair = addBooleanSetting("Require Crosshair", "Only attack when crosshair is on target", true);
    }

    @Override
    public void onDisable() {
        delayCounter = 0;
    }


    @Override
    public void onTick() {
        if (!this.isEnabled()) return;

        // Decrease delay counter
        if (delayCounter > 0) {
            delayCounter--;
            return;
        }

        // Check if we're looking at a player and can attack
        if (PlayerUtils.isLookingAtPlayer()) {
            PlayerEntity target = PlayerUtils.getTargetPlayer();
            if (target != null && mc.player != null) {

                // Anti-detection: Only attack if crosshair requirement is met
                if (RequireCrosshair.getValue()) {
                    // Check if we're actually looking directly at the target
                    HitResult hitResult = mc.crosshairTarget;
                    if (!(hitResult instanceof EntityHitResult) ||
                            ((EntityHitResult) hitResult).getEntity() != target) {
                        return; // Don't attack if not directly looking
                    }
                }

                // Check if we can attack (cooldown is ready)
                if (mc.player.getAttackCooldownProgress(0.5f) >= 0.9f) {

                    // Anti-detection: Add slight mouse movement variation
                    float yawVariation = (random.nextFloat() - 0.5f) * 2.0f; // ±1 degree
                    float pitchVariation = (random.nextFloat() - 0.5f) * 1.5f; // ±0.75 degree

                    // Apply small random rotation (simulate human imperfection)
                    mc.player.setYaw(mc.player.getYaw() + yawVariation);
                    mc.player.setPitch(mc.player.getPitch() + pitchVariation);

                    // Get the player's active hand
                    Hand activeHand = mc.player.getActiveHand();
                    if (activeHand == null) {
                        // If no active hand, use main hand as default
                        activeHand = Hand.MAIN_HAND;
                    }

                    // Attack the target player
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(activeHand);

                    // Anti-detection: Randomized delay
                    int baseDelay = (int) (EquipDelay.getValue() / 50);
                    int randomDelay = (int) ((random.nextFloat() - 0.5f) * 2 * DelayRandomization.getValue() / 50);
                    delayCounter = Math.max(1, baseDelay + randomDelay);

                    // Anti-detection: Occasionally miss attacks (simulate human error)
                    if (random.nextFloat() < 0.02f) { // 2% chance to "miss"
                        delayCounter += 10; // Add extra delay as if reacting to miss
                    }
                }
            }
        }
    }}