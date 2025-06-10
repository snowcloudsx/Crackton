package me.snow.impl;

import me.snow.mixin.HandledScreenAccessor;
import me.snow.notifications.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HoverTotem extends Module {

    private static final int DELAY_TICKS = 10;
    private int delayCounter = 0;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public HoverTotem() {
        super("Hover Totem", "Offhands a hovered totem in inventory", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        delayCounter = 0;
    }

    @Override
    public void onDisable() {
        delayCounter = 0;
        scheduler.shutdown();
        // Cancel any ongoing interactions when disabled
        if (mc.player != null && mc.interactionManager != null) {
            // Drop cursor stack if holding something
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        -999,
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                );
            }
        }
    }

    @Override
    public void onTick() {
        if (!this.isEnabled()) return; // Fixed logic - return if NOT enabled

        if (delayCounter > 0) {
            delayCounter--;
            return;
        }
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) return;
        if (mc.player == null || mc.interactionManager == null) return;

        if (!mc.player.getOffHandStack().isEmpty()) return;

        Slot hoveredSlot = ((HandledScreenAccessor)(Object)screen).getFocusedSlot();
        if (hoveredSlot == null || !hoveredSlot.hasStack()) return;

        ItemStack hovered = hoveredSlot.getStack();
        if (!hovered.isOf(Items.TOTEM_OF_UNDYING)) return;

        moveHoveredItemToOffhand(hoveredSlot.id);
        delayCounter = DELAY_TICKS;
    }

    private void moveHoveredItemToOffhand(int fromSlot) {
        int syncId = mc.player.currentScreenHandler.syncId;

        // Step 1: Pickup hovered item
        mc.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);

        // Random delay between 100-175ms before placing in offhand
        int delay1 = 100 + random.nextInt(76); // 100 + (0-75) = 100-175ms
        scheduler.schedule(() -> {
            if (!this.isEnabled() || mc.player == null || mc.interactionManager == null) return;

            // Step 2: Place into offhand (slot 45)
            mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);

            // Random delay between 100-175ms before putting back remaining items
            int delay2 = 100 + random.nextInt(76);
            scheduler.schedule(() -> {
                if (!this.isEnabled() || mc.player == null || mc.interactionManager == null) return;

                // Step 3: Put back anything left on cursor
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
                }
            }, delay2, TimeUnit.MILLISECONDS);

        }, delay1, TimeUnit.MILLISECONDS);
    }
}