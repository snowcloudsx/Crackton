package me.snow.impl.player;

import net.minecraft.client.MinecraftClient;
import me.snow.impl.Module;
import me.snow.impl.Category;
import me.snow.mixin.FastplaceAccessor;

public class Fastplace extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Fastplace() {
        super("FastPlace", "Place Blocks Fast", Category.CLIENT);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        // Restore normal cooldown when disabled
        if (mc != null) {
            ((FastplaceAccessor) mc).setItemUseCooldown(4); // Default Minecraft cooldown
        }
    }

    @Override
    public void onTick() {
        if (mc != null && mc.player != null && this.isEnabled()) {
            // Only set cooldown to 0 when module is enabled
            ((FastplaceAccessor) mc).setItemUseCooldown(0);
        }
    }
}