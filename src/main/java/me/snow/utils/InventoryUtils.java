package me.snow.utils;

import me.snow.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    /**
     * Get the currently hovered slot using mixin accessor.
     */
    public static Slot getHoveredSlot() {
        if (!(MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen)) {
            return null;
        }
        return ((HandledScreenAccessor) screen).getFocusedSlot();
    }


    /**
     * Get the itemstack currently hovered in any container.
     */
    public static ItemStack getHoveredItem() {
        Slot slot = getHoveredSlot();
        if (slot != null && slot.hasStack()) {
            return slot.getStack();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get the offhand itemstack if the hovered slot is the offhand slot.
     */
    public static ItemStack getOffhandHoveredItem() {
        Slot slot = getHoveredSlot();
        if (slot != null && slot.getIndex() == 45 && slot.hasStack()) {
            return slot.getStack();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Drop the currently hovered item stack (one item).
     */
    public static void dropHoveredItem() {
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;
        Slot hovered = getHoveredSlot();
        if (hovered != null && hovered.hasStack()) {
            client.interactionManager.clickSlot(
                    screen.getScreenHandler().syncId, // Use the screen handler's syncId
                    hovered.id,
                    1, // drop one item
                    SlotActionType.THROW,
                    client.player
            );
        }
    }


    /**
     * Get item in offhand.
     */
    public static ItemStack getOffhandItem() {
        return client.player != null ? client.player.getOffHandStack() : ItemStack.EMPTY;
    }

    /**
     * Drop offhand item.
     */
    public static void dropOffhandItem() {
        if (client.player != null && !client.player.getOffHandStack().isEmpty()) {
            // 45 is the offhand slot index in player inventory
            client.interactionManager.clickSlot(
                    0,
                    45,
                    1,
                    SlotActionType.THROW,
                    client.player
            );
        }
    }

    /**
     * Get all items in the player's inventory (main + armor + offhand).
     */
    public static List<ItemStack> getAllInventoryItems() {
        List<ItemStack> all = new ArrayList<>();
        if (client.player != null) {
            PlayerInventory inv = client.player.getInventory();
            all.addAll(inv.main);
            all.addAll(inv.armor);
            all.add(inv.offHand.get(0));
        }
        return all;
    }

    /**
     * Get only main inventory items (excluding armor/offhand).
     */
    public static List<ItemStack> getMainInventoryItems() {
        if (client.player == null) return new ArrayList<>();
        return new ArrayList<>(client.player.getInventory().main);
    }

    /**
     * Get items in hotbar (slots 0-8).
     */
    public static List<ItemStack> getHotbarItems() {
        List<ItemStack> hotbar = new ArrayList<>();
        if (client.player != null) {
            for (int i = 0; i < 9; i++) {
                hotbar.add(client.player.getInventory().getStack(i));
            }
        }
        return hotbar;
    }
}
