package me.snow.impl.Render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import me.snow.impl.Module;
import me.snow.impl.Category;

public class AntiTrap extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AntiTrap() {
        super("AntiTrap", "Removes trap entities like Armor Stands and Chest Minecarts", Category.RENDER);
    }

    @Override
    public void onEnable() {
        removeExistingTraps();
    }

    @Override
    public void onDisable() {
        removeExistingTraps();
    }

    @Override
    public void onTick() {
        try {
            if (mc == null || mc.world == null || mc.getNetworkHandler() == null) return;

            ClientWorld world = mc.world;
            for (Entity entity : world.getEntities()) {
                try {
                    if (isTrapEntity(entity.getType()) && !entity.isRemoved()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // log per-entity issues
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // log outer loop issues
        }
    }

    private void removeExistingTraps() {
        try {
            if (mc == null || mc.world == null) return;

            for (Entity entity : mc.world.getEntities()) {
                try {
                    if (isTrapEntity(entity.getType()) && !entity.isRemoved()) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // log per-entity issues
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // log outer loop issues
        }
    }

    private boolean isTrapEntity(EntityType<?> type) {
        return type == EntityType.ARMOR_STAND || type == EntityType.CHEST_MINECART;
    }
}
