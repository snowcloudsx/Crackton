package me.snow.impl;

import me.snow.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class HoverTotem extends Module {

    private static final int DELAY_TICKS = 10;
    private int delayCounter = 0;

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
    }

    @Override
    public void onTick() {
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

        // Pickup hovered item
        mc.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);

        // Place into offhand (slot 45)
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);

        // Put back anything left on cursor
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }
}
