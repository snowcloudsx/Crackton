package me.snow.impl.misc;

import me.snow.utils.InventoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import me.snow.impl.Category;
import me.snow.impl.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoneDropper extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int delayTicks = 0;
    private int currentDelay = 0;
    private final Random random = new Random();
    private boolean inventoryOpenLastTick = false;
    private List<Slot> boneSlots = new ArrayList<>();
    private int currentSlotIndex = 0;

    public BoneDropper() {
        super("AutoBoneDropper", "Drops all bones automatically when inventory is opened with a delay per stack", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        resetDropState();
    }

    @Override
    public void onDisable() {
        resetDropState();

        // Clear cursor if holding any item when disabling
        if (mc.player != null && mc.interactionManager != null) {
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
        Screen current = mc.currentScreen;
        boolean inventoryOpen = current instanceof HandledScreen<?>;

        if (inventoryOpen && !inventoryOpenLastTick) {
            // Inventory just opened, reset state
            resetDropState();
            boneSlots = getBoneSlots((HandledScreen<?>) current);
        }
        inventoryOpenLastTick = inventoryOpen;

        if (inventoryOpen && current instanceof HandledScreen<?> screen) {
            if (currentSlotIndex < boneSlots.size()) {
                if (delayTicks >= currentDelay) {
                    Slot slotToDrop = boneSlots.get(currentSlotIndex);
                    if (slotToDrop.hasStack()) {
                        dropFullStack(screen, slotToDrop);
                    }
                    currentSlotIndex++;
                    delayTicks = 0;
                    currentDelay = random.nextInt(2) + 5; // 5-9 ticks delay per stack
                } else {
                    delayTicks++;
                }
            }
        }
    }

    private void resetDropState() {
        delayTicks = 0;
        currentDelay = random.nextInt(2) + 5;
        currentSlotIndex = 0;
        boneSlots.clear();
    }

    private List<Slot> getBoneSlots(HandledScreen<?> screen) {
        List<Slot> bones = new ArrayList<>();
        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.isOf(Items.BONE)) {
                bones.add(slot);
            }
        }
        return bones;
    }

    private void dropFullStack(HandledScreen<?> screen, Slot slot) {
        mc.interactionManager.clickSlot(
                screen.getScreenHandler().syncId,
                slot.id,
                1, // button 0 (left click)
                SlotActionType.THROW, // shift-click to quick-move (drop whole stack)
                mc.player
        );

    }
}
