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
        if (mc == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // Listen for newly spawned entities and remove if trap
        ClientWorld world = mc.world;
        for (Entity entity : world.getEntities()) {
            if (isTrapEntity(entity.getType()) && !entity.isRemoved()) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private void removeExistingTraps() {
        if (mc == null || mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (isTrapEntity(entity.getType()) && !entity.isRemoved()) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private boolean isTrapEntity(EntityType<?> type) {
        return type == EntityType.ARMOR_STAND || type == EntityType.CHEST_MINECART;
    }
}
