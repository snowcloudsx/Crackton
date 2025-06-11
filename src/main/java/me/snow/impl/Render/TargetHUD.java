package me.snow.impl.Render;

import me.snow.impl.Category;
import me.snow.impl.Module;
import me.snow.notifications.NotificationManager;
import me.snow.utils.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TargetHUD extends Module {

    private static final int DELAY_TICKS = 10;
    private static final double MAX_TRACKING_DISTANCE = 100.0;
    private static final long INTERACTION_TIMEOUT = 300000; // 5 minutes in milliseconds

    private int delayCounter = 0;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // Track players we've interacted with
    private final Map<String, Long> interactedPlayers = new HashMap<>();
    private boolean wasSwinging = false;

    public TargetHUD() {
        super("TargetHUD", "Display target player info", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        delayCounter = 0;
        wasSwinging = false;
    }

    @Override
    protected void initSettings() {
    }

    @Override
    public void onDisable() {
        delayCounter = 0;
        wasSwinging = false;
        interactedPlayers.clear();
    }

    @Override
    public void onTick() {
        if (!this.isEnabled()) return;

        // Track swing interactions
        trackPlayerInteractions();

        // Clean up old interactions
        cleanupOldInteractions();

        if (delayCounter > 0) {
            delayCounter--;
            return;
        }

        PlayerEntity target = getValidTarget();
        if (target != null && mc.player != null) {
            displayTargetInfo(target);
        }

        delayCounter = DELAY_TICKS;
    }

    private void trackPlayerInteractions() {
        if (mc.player == null) return;

        boolean isSwinging = mc.player.handSwinging;

        // Detect new swing (transition from not swinging to swinging)
        if (isSwinging && !wasSwinging) {
            PlayerEntity target = PlayerUtils.getTargetPlayer();
            if (target != null) {
                String playerName = target.getDisplayName().getString();
                interactedPlayers.put(playerName, System.currentTimeMillis());
            }
        }

        wasSwinging = isSwinging;
    }

    private void cleanupOldInteractions() {
        long currentTime = System.currentTimeMillis();
        interactedPlayers.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > INTERACTION_TIMEOUT
        );
    }

    private PlayerEntity getValidTarget() {
        PlayerEntity target = PlayerUtils.getTargetPlayer();
        if (target == null) return null;

        double distance = mc.player.distanceTo(target);
        String playerName = target.getDisplayName().getString();

        // Show target if within normal range OR if we've interacted with them and within 100 blocks
        if (distance <= 16.0 || (interactedPlayers.containsKey(playerName) && distance <= MAX_TRACKING_DISTANCE)) {
            return target;
        }

        return null;
    }

    private void displayTargetInfo(PlayerEntity target) {
        // Get properly formatted display name
        String playerName = target.getDisplayName().getString();

        // Format health with one decimal place
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        String healthText = String.format("%.1f/%.1f", health, maxHealth);

        // Get active item name safely
        String activeItemName = "None";
        ItemStack activeItem = target.getActiveItem();
        if (!activeItem.isEmpty()) {
            Text itemName = activeItem.getName();
            if (itemName != null) {
                activeItemName = itemName.getString();
            }
        }

        // Get main hand item if no active item
        String mainHandItemName = "None";
        ItemStack mainHandItem = target.getMainHandStack();
        if (!mainHandItem.isEmpty()) {
            Text itemName = mainHandItem.getName();
            if (itemName != null) {
                mainHandItemName = itemName.getString();
            }
        }

        // Build the display string with proper formatting
        StringBuilder displayText = new StringBuilder();

        // Add interaction indicator
        if (interactedPlayers.containsKey(playerName)) {
            displayText.append("§6[T] ");
        }

        displayText.append("§c").append(playerName).append(" ");
        displayText.append("§f[§a").append(healthText).append("§f] ");

        // Show active item if using something, otherwise show main hand
        if (!activeItem.isEmpty()) {
            displayText.append("§7Active: §e").append(activeItemName);
        } else {
            displayText.append("§7Holding: §e").append(mainHandItemName);
        }

        // Add distance information
        double distance = mc.player.distanceTo(target);
        displayText.append(" §7(§b").append(String.format("%.1fm", distance)).append("§7)");

        NotificationManager.sendActionBar(displayText.toString());
    }
}