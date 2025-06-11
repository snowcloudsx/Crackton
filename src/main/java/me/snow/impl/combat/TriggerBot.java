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
    private BooleanSetting PrioritizeCriticals; // NEW
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
        EquipDelay = addNumberSetting("Hit Delay", "Sets the Delay before You Hit (ms)", 600, 50, 2000);
        DelayRandomization = addNumberSetting("Delay Randomization", "Random delay variation (ms)", 100, 0, 300);
        RequireCrosshair = addBooleanSetting("Require Crosshair", "Only attack when crosshair is on target", true);
        PrioritizeCriticals = addBooleanSetting("Prioritize Criticals", "Wait for jump to be in air before hitting", false); // NEW
    }

    @Override
    public void onDisable() {
        delayCounter = 0;
    }


    @Override
    public void onTick() {
        if (!this.isEnabled()) return;

        if (delayCounter > 0) {
            delayCounter--;
            return;
        }

        if (PlayerUtils.isLookingAtPlayer()) {
            PlayerEntity target = PlayerUtils.getTargetPlayer();
            if (target != null && mc.player != null) {

                if (RequireCrosshair.getValue()) {
                    HitResult hitResult = mc.crosshairTarget;
                    if (!(hitResult instanceof EntityHitResult) ||
                            ((EntityHitResult) hitResult).getEntity() != target) {
                        return;
                    }
                }

                // Wait for cooldown
                if (mc.player.getAttackCooldownProgress(0.5f) >= 0.9f) {

                    // --- NEW: Critical hit check ---
                    if (PrioritizeCriticals.getValue()) {
                        boolean isInAir = !mc.player.isOnGround() && mc.player.getVelocity().y < -0.1;
                        boolean isFlatOnGround = mc.player.isOnGround();
                        if (!isInAir && !isFlatOnGround) {
                            return; // Not ready for critical hit and not on ground, skip
                        }
                    }

                    // Yaw/pitch variation
                    float yawVariation = (random.nextFloat() - 0.5f) * 2.0f;
                    float pitchVariation = (random.nextFloat() - 0.5f) * 1.5f;
                    mc.player.setYaw(mc.player.getYaw() + yawVariation);
                    mc.player.setPitch(mc.player.getPitch() + pitchVariation);

                    Hand activeHand = mc.player.getActiveHand();
                    if (activeHand == null) activeHand = Hand.MAIN_HAND;

                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(activeHand);

                    int baseDelay = (int) (EquipDelay.getValue() / 50);
                    int randomDelay = (int) ((random.nextFloat() - 0.5f) * 2 * DelayRandomization.getValue() / 50);
                    delayCounter = Math.max(1, baseDelay + randomDelay);

                    if (random.nextFloat() < 0.02f) delayCounter += 10;
                }
            }
        }
    }}