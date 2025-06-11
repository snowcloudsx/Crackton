package me.snow.impl.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import me.snow.impl.Module;
import me.snow.impl.Category;

public class AutoFirework extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private long lastFireworkTime = 0;
    private long lastGlidingTime = 0;
    private boolean wasGliding = false;
    private float averageVelocity = 0.0f;
    private int velocitySamples = 0;
    private boolean isMovingFirework = false;
    private int inventoryTaskTicks = 0;

    // Configuration
    private static final long FIREWORK_COOLDOWN = 2000; // 2 seconds between fireworks
    private static final long MIN_GLIDING_TIME = 3000; // Must be gliding for 3 seconds before first firework
    private static final long GLIDING_FIREWORK_INTERVAL = 4000; // 8 seconds between fireworks while gliding
    private static final double VELOCITY_THRESHOLD = 0.8; // Minimum velocity to maintain
    private static final double LOW_VELOCITY_THRESHOLD = 0.4; // Critically low velocity

    public AutoFirework() {
        super("AutoFirework", "Automatically uses fireworks with improved timing", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        resetTracking();
    }

    @Override
    public void onDisable() {
        resetTracking();
        // Close inventory if we opened it
        if (mc.currentScreen instanceof InventoryScreen) {
            mc.player.closeHandledScreen();
        }
    }

    private void resetTracking() {
        lastFireworkTime = 0;
        lastGlidingTime = 0;
        wasGliding = false;
        averageVelocity = 0.0f;
        velocitySamples = 0;
        isMovingFirework = false;
        inventoryTaskTicks = 0;
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null || mc.world == null || !this.isEnabled()) {
            return;
        }

        PlayerEntity player = mc.player;
        long currentTime = System.currentTimeMillis();
        boolean isGliding = player.isGliding();

        // Handle inventory firework moving task
        if (isMovingFirework) {
            handleInventoryTask();
            return;
        }

        // Track gliding state changes
        if (isGliding && !wasGliding) {
            lastGlidingTime = currentTime;
            resetVelocityTracking();
        }
        wasGliding = isGliding;

        // Update velocity tracking
        if (isGliding) {
            updateVelocityTracking(player);
        }

        // Check if we should use firework
        if (shouldUseFirework(player, currentTime, isGliding)) {
            if (hasFireworkInHands()) {
                useFirework();
                lastFireworkTime = currentTime;
            } else if (hasFireworkInHotbar()) {
                // Switch to firework slot if it's already in hotbar
                switchToFireworkSlot();
            } else if (hasFireworkInInventory() && !isMovingFirework) {
                startFireworkMove();
            }
        }
    }

    private boolean shouldUseFirework(PlayerEntity player, long currentTime, boolean isGliding) {
        // Check basic cooldown
        if (currentTime - lastFireworkTime < FIREWORK_COOLDOWN) {
            return false;
        }

        // Condition 1: Gliding with time-based logic
        if (isGliding) {
            long glidingDuration = currentTime - lastGlidingTime;

            // First firework after gliding for MIN_GLIDING_TIME
            if (glidingDuration >= MIN_GLIDING_TIME && lastFireworkTime < lastGlidingTime) {
                return averageVelocity < VELOCITY_THRESHOLD;
            }

            // Subsequent fireworks based on interval and velocity
            if (glidingDuration >= MIN_GLIDING_TIME &&
                    currentTime - lastFireworkTime >= GLIDING_FIREWORK_INTERVAL) {
                return averageVelocity < VELOCITY_THRESHOLD;
            }

            // Emergency firework if velocity is critically low
            if (glidingDuration >= MIN_GLIDING_TIME && averageVelocity < LOW_VELOCITY_THRESHOLD) {
                return currentTime - lastFireworkTime >= FIREWORK_COOLDOWN;
            }
        }

        // Condition 2: Emergency situations (falling, low health)
        if (!isGliding) {
            // Falling fast without elytra
            if (player.getVelocity().y < -1.5 && player.fallDistance > 15.0f) {
                return true;
            }

            // Low health and falling
            if (player.getHealth() <= 6.0f && player.getVelocity().y < -0.5) {
                return true;
            }
        }

        return false;
    }

    private void updateVelocityTracking(PlayerEntity player) {
        double horizontalSpeed = Math.sqrt(
                player.getVelocity().x * player.getVelocity().x +
                        player.getVelocity().z * player.getVelocity().z
        );

        // Rolling average of velocity
        if (velocitySamples < 10) {
            averageVelocity = (averageVelocity * velocitySamples + (float)horizontalSpeed) / (velocitySamples + 1);
            velocitySamples++;
        } else {
            averageVelocity = averageVelocity * 0.9f + (float)horizontalSpeed * 0.1f;
        }
    }

    private void resetVelocityTracking() {
        averageVelocity = 0.0f;
        velocitySamples = 0;
    }

    private boolean hasFireworkInHands() {
        if (mc.player == null) return false;

        return mc.player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.FIREWORK_ROCKET ||
                mc.player.getStackInHand(Hand.OFF_HAND).getItem() == Items.FIREWORK_ROCKET;
    }

    private boolean hasFireworkInHotbar() {
        if (mc.player == null) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFireworkInInventory() {
        if (mc.player == null) return false;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return true;
            }
        }
        return false;
    }

    private void useFirework() {
        if (mc.player == null || mc.interactionManager == null) return;

        Hand handToUse = null;

        if (mc.player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.FIREWORK_ROCKET) {
            handToUse = Hand.MAIN_HAND;
        } else if (mc.player.getStackInHand(Hand.OFF_HAND).getItem() == Items.FIREWORK_ROCKET) {
            handToUse = Hand.OFF_HAND;
        }

        if (handToUse != null) {
            mc.interactionManager.interactItem(mc.player, handToUse);
        }
    }

    private void startFireworkMove() {
        if (mc.player == null || isMovingFirework) return;
//idk imay just stick with this
        // Double-check we don't already have fireworks in hands
        if (hasFireworkInHands()) {
            return;
        }

        // Open inventory
        mc.setScreen(new InventoryScreen(mc.player));
        isMovingFirework = true;
        inventoryTaskTicks = 0;
    }

    private void handleInventoryTask() {
        inventoryTaskTicks++;

        // Wait a few ticks for inventory to open
        if (inventoryTaskTicks < 3) {
            return;
        }

        // Check if inventory is open
        if (!(mc.currentScreen instanceof InventoryScreen)) {
            isMovingFirework = false;
            return;
        }

        // Find firework in inventory
        int fireworkSlot = findFireworkSlot();
        if (fireworkSlot == -1) {
            closeInventoryAndReset();
            return;
        }

        // Find target slot (prefer main hand slot 0, or first empty hotbar slot)
        int targetSlot = findBestTargetSlot();
        if (targetSlot == -1) {
            closeInventoryAndReset();
            return;
        }

        // Perform the slot click to move firework
        if (mc.interactionManager != null && mc.player.currentScreenHandler != null) {
            // Click source slot
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    fireworkSlot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );

            // Wait a tick then click target slot
            if (inventoryTaskTicks >= 5) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        targetSlot,
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                );

                // Close inventory after successful move
                closeInventoryAndReset();

                // Switch to the firework slot if it's in hotbar
                switchToFireworkSlot();

                // Try to use the firework immediately if conditions are still met
                if (hasFireworkInHands() && shouldUseFirework(mc.player, System.currentTimeMillis(), mc.player.isGliding())) {
                    useFirework();
                    lastFireworkTime = System.currentTimeMillis();
                };
            }
        }

        // Timeout after 10 ticks
        if (inventoryTaskTicks >= 10) {
            closeInventoryAndReset();
        }
    }

    private int findFireworkSlot() {
        if (mc.player == null) return -1;

        // Check main inventory slots (9-35 in container)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }

    private int findBestTargetSlot() {
        if (mc.player == null) return -1;

        // Prefer hotbar slots over main hand for easier switching
        // Check hotbar slots 0-8 (container slots 36-44)
        for (int i = 36; i < 45; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.isEmpty()) {
                return i;
            }
        }

        // If no empty slots, use main hand (slot 36)
        return 36;
    }

    private void switchToFireworkSlot() {
        if (mc.player == null) return;

        // Find which hotbar slot has the firework
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                // Switch to this slot
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    private void closeInventoryAndReset() {
        if (mc.currentScreen instanceof InventoryScreen) {
            mc.player.closeHandledScreen();
        }
        isMovingFirework = false;
        inventoryTaskTicks = 0;

        // Add a small delay before next firework attempt
        lastFireworkTime = System.currentTimeMillis() - FIREWORK_COOLDOWN + 500;
    }
}