package me.snow.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ============ BASIC PLAYER INFO ============

    /**
     * Get the current client player
     */
    public static ClientPlayerEntity getPlayer() {
        return mc.player;
    }

    /**
     * Check if player exists and is in world
     */
    public static boolean isPlayerValid() {
        return mc.player != null && mc.world != null;
    }

    /**
     * Get player's current position
     */
    public static Vec3d getPlayerPos() {
        return isPlayerValid() ? mc.player.getPos() : Vec3d.ZERO;
    }

    /**
     * Get player's eye position
     */
    public static Vec3d getEyePos() {
        return isPlayerValid() ? mc.player.getEyePos() : Vec3d.ZERO;
    }

    /**
     * Get player's block position
     */
    public static BlockPos getBlockPos() {
        return isPlayerValid() ? mc.player.getBlockPos() : BlockPos.ORIGIN;
    }

    /**
     * Get player's name
     */
    public static String getPlayerName() {
        return isPlayerValid() ? mc.player.getName().getString() : "Unknown";
    }

    /**
     * Get player's UUID
     */
    public static UUID getPlayerUUID() {
        return isPlayerValid() ? mc.player.getUuid() : null;
    }

    // ============ PLAYER STATS ============

    /**
     * Get player's health
     */
    public static float getHealth() {
        return isPlayerValid() ? mc.player.getHealth() : 0.0f;
    }

    /**
     * Get player's max health
     */
    public static float getMaxHealth() {
        return isPlayerValid() ? mc.player.getMaxHealth() : 20.0f;
    }

    /**
     * Get player's hunger level
     */
    public static int getHunger() {
        return isPlayerValid() ? mc.player.getHungerManager().getFoodLevel() : 0;
    }

    /**
     * Get player's saturation
     */
    public static float getSaturation() {
        return isPlayerValid() ? mc.player.getHungerManager().getSaturationLevel() : 0.0f;
    }

    /**
     * Get player's experience level
     */
    public static int getExpLevel() {
        return isPlayerValid() ? mc.player.experienceLevel : 0;
    }

    /**
     * Get player's experience progress
     */
    public static float getExpProgress() {
        return isPlayerValid() ? mc.player.experienceProgress : 0.0f;
    }

    /**
     * Check if player is on ground
     */
    public static boolean isOnGround() {
        return isPlayerValid() && mc.player.isOnGround();
    }

    /**
     * Check if player is in water
     */
    public static boolean isInWater() {
        return isPlayerValid() && mc.player.isTouchingWater();
    }

    /**
     * Check if player is in lava
     */
    public static boolean isInLava() {
        return isPlayerValid() && mc.player.isInLava();
    }

    /**
     * Check if player is sneaking
     */
    public static boolean isSneaking() {
        return isPlayerValid() && mc.player.isSneaking();
    }

    /**
     * Check if player is sprinting
     */
    public static boolean isSprinting() {
        return isPlayerValid() && mc.player.isSprinting();
    }

    /**
     * Check if player is flying
     */
    public static boolean isFlying() {
        return isPlayerValid() && mc.player.getAbilities().flying;
    }

    // ============ INVENTORY UTILS ============

    /**
     * Get player's inventory
     */
    public static PlayerInventory getInventory() {
        return isPlayerValid() ? mc.player.getInventory() : null;
    }

    /**
     * Get item in main hand
     */
    public static ItemStack getMainHandItem() {
        return isPlayerValid() ? mc.player.getMainHandStack() : ItemStack.EMPTY;
    }

    /**
     * Get item in off hand
     */
    public static ItemStack getOffHandItem() {
        return isPlayerValid() ? mc.player.getOffHandStack() : ItemStack.EMPTY;
    }

    /**
     * Get currently selected hotbar slot
     */
    public static int getSelectedSlot() {
        return isPlayerValid() ? mc.player.getInventory().selectedSlot : 0;
    }

    /**
     * Count items of a specific type in inventory
     */
    public static int countItems(ItemStack itemStack) {
        if (!isPlayerValid() || itemStack.isEmpty()) return 0;

        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (ItemStack.areItemsEqual(stack, itemStack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Check if inventory is full
     */
    public static boolean isInventoryFull() {
        if (!isPlayerValid()) return false;

        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // ============ MOVEMENT & ROTATION ============

    /**
     * Get player's velocity
     */
    public static Vec3d getVelocity() {
        return isPlayerValid() ? mc.player.getVelocity() : Vec3d.ZERO;
    }

    /**
     * Get player's speed (horizontal)
     */
    public static double getSpeed() {
        if (!isPlayerValid()) return 0.0;
        Vec3d vel = mc.player.getVelocity();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
    }

    /**
     * Get player's yaw rotation
     */
    public static float getYaw() {
        return isPlayerValid() ? mc.player.getYaw() : 0.0f;
    }

    /**
     * Get player's pitch rotation
     */
    public static float getPitch() {
        return isPlayerValid() ? mc.player.getPitch() : 0.0f;
    }

    /**
     * Get player's look direction vector
     */
    public static Vec3d getLookDirection() {
        return isPlayerValid() ? mc.player.getRotationVector() : Vec3d.ZERO;
    }

    /**
     * Calculate distance to a position
     */
    public static double distanceTo(Vec3d pos) {
        return isPlayerValid() ? mc.player.getPos().distanceTo(pos) : Double.MAX_VALUE;
    }

    /**
     * Calculate distance to a block position
     */
    public static double distanceTo(BlockPos pos) {
        return distanceTo(Vec3d.ofCenter(pos));
    }

    /**
     * Calculate squared distance (faster for comparisons)
     */
    public static double squaredDistanceTo(Vec3d pos) {
        return isPlayerValid() ? mc.player.getPos().squaredDistanceTo(pos) : Double.MAX_VALUE;
    }

    // ============ TARGETING & DETECTION ============

    /**
     * Get the entity the player is looking at
     */
    public static Entity getTargetEntity() {
        if (!isPlayerValid() || mc.crosshairTarget == null) return null;

        if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) mc.crosshairTarget).getEntity();
        }
        return null;
    }

    /**
     * Get the player the client is looking at
     */
    public static PlayerEntity getTargetPlayer() {
        Entity target = getTargetEntity();
        return target instanceof PlayerEntity ? (PlayerEntity) target : null;
    }

    /**
     * Check if looking at a player
     */
    public static boolean isLookingAtPlayer() {
        return getTargetPlayer() != null;
    }

    /**
     * Get all players within range
     */
    public static List<PlayerEntity> getPlayersInRange(double range) {
        List<PlayerEntity> players = new ArrayList<>();
        if (!isPlayerValid()) return players;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && mc.player.distanceTo(player) <= range) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Get closest player to client player
     */
    public static PlayerEntity getClosestPlayer() {
        if (!isPlayerValid()) return null;

        PlayerEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player) {
                double dist = mc.player.distanceTo(player);
                if (dist < closestDist) {
                    closest = player;
                    closestDist = dist;
                }
            }
        }
        return closest;
    }

    // ============ MULTIPLAYER UTILS ============

    /**
     * Get all online players
     */
    public static List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        if (mc.getNetworkHandler() == null) return names;

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            names.add(entry.getProfile().getName());
        }
        return names;
    }

    /**
     * Get player count
     */
    public static int getPlayerCount() {
        return mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerList().size() : 0;
    }

    /**
     * Check if a player is online
     */
    public static boolean isPlayerOnline(String playerName) {
        return getOnlinePlayerNames().contains(playerName);
    }

    /**
     * Get player's ping
     */
    public static int getPing() {
        if (!isPlayerValid() || mc.getNetworkHandler() == null) return 0;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }

    /**
     * Get player's game mode
     */
    public static GameMode getGameMode() {
        if (!isPlayerValid() || mc.getNetworkHandler() == null) return GameMode.SURVIVAL;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry != null ? entry.getGameMode() : GameMode.SURVIVAL;
    }

    // ============ UTILITY FUNCTIONS ============

    /**
     * Check if player can see the sky
     */
    public static boolean canSeeSky() {
        return isPlayerValid() && mc.world.isSkyVisible(mc.player.getBlockPos());
    }

    /**
     * Get light level at player position
     */
    public static int getLightLevel() {
        return isPlayerValid() ? mc.world.getLightLevel(mc.player.getBlockPos()) : 0;
    }

    /**
     * Calculate angle between player and target position
     */
    public static float[] getAnglesToPos(Vec3d target) {
        if (!isPlayerValid()) return new float[]{0.0f, 0.0f};

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d diff = target.subtract(eyePos);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, dist));

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
    }

    /**
     * Check if player is moving
     */
    public static boolean isMoving() {
        return isPlayerValid() && (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0);
    }

    /**
     * Get player's fall distance
     */
    public static float getFallDistance() {
        return isPlayerValid() ? mc.player.fallDistance : 0.0f;
    }

    /**
     * Check if player is in creative mode
     */
    public static boolean isCreative() {
        return getGameMode() == GameMode.CREATIVE;
    }

    /**
     * Check if player is in spectator mode
     */
    public static boolean isSpectator() {
        return getGameMode() == GameMode.SPECTATOR;
    }

    /**
     * Check if player has creative flight
     */
    public static boolean canFly() {
        return isPlayerValid() && mc.player.getAbilities().allowFlying;
    }
}