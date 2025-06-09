package me.snow.notifications;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NotificationManager {

    // ==================== CHAT NOTIFICATIONS ====================

    /**
     * Shows a basic chat message notification
     */
    public static void sendChatMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("[Crackton] " + message).formatted(Formatting.AQUA),
                    false
            );
        }
    }

    /**
     * Shows a chat message with custom formatting
     */
    public static void sendChatMessage(String message, Formatting color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("[Crackton] " + message).formatted(color),
                    false
            );
        }
    }

    /**
     * Shows an action bar message (above hotbar)
     */
    public static void sendActionBar(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal(message).formatted(Formatting.YELLOW),
                    true // true = action bar
            );
        }
    }

    /**
     * Shows an action bar message with custom color
     */
    public static void sendActionBar(String message, Formatting color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal(message).formatted(color),
                    true
            );
        }
    }

    // ==================== TOAST NOTIFICATIONS ====================

    /**
     * Shows a basic toast notification (top-right corner)
     */
    public static void showToast(String title, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        ToastManager toastManager = client.getToastManager();

        SystemToast.add(
                toastManager,
                SystemToast.Type.NARRATOR_TOGGLE,
                Text.literal(title),
                Text.literal(description)
        );
    }

    /**
     * Shows a tutorial hint toast
     */


    /**
     * Shows a world backup toast
     */
    public static void showBackupToast(String title, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        ToastManager toastManager = client.getToastManager();

        SystemToast.add(
                toastManager,
                SystemToast.Type.WORLD_BACKUP,
                Text.literal(title),
                Text.literal(description)
        );
    }

    /**
     * Shows a world access failed toast
     */
    public static void showAccessFailedToast(String title, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        ToastManager toastManager = client.getToastManager();

        SystemToast.add(
                toastManager,
                SystemToast.Type.WORLD_ACCESS_FAILURE,
                Text.literal(title),
                Text.literal(description)
        );
    }

    /**
     * Shows a pack load failure toast
     */
    public static void showPackFailureToast(String title, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        ToastManager toastManager = client.getToastManager();

        SystemToast.add(
                toastManager,
                SystemToast.Type.PACK_LOAD_FAILURE,
                Text.literal(title),
                Text.literal(description)
        );
    }

    // ==================== TITLE NOTIFICATIONS ====================

    /**
     * Shows a title and subtitle on screen (large text in center)
     */
    public static void showTitle(String title, String subtitle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.setTitle(Text.literal(title));
            client.inGameHud.setSubtitle(Text.literal(subtitle));
        }
    }

    /**
     * Shows a title with custom formatting
     */
    public static void showTitle(String title, Formatting titleColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.setTitle(Text.literal(title).formatted(titleColor));
        }
    }

    /**
     * Shows a title and subtitle with custom formatting
     */
    public static void showTitle(String title, String subtitle, Formatting titleColor, Formatting subtitleColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.setTitle(Text.literal(title).formatted(titleColor));
            client.inGameHud.setSubtitle(Text.literal(subtitle).formatted(subtitleColor));
        }
    }

    /**
     * Shows just a title on screen
     */
    public static void showTitle(String title) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.setTitle(Text.literal(title));
        }
    }

    /**
     * Shows title with timing control (fadeIn, stay, fadeOut in ticks)
     */
    public static void showTitleWithTiming(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.setTitleTicks(fadeIn, stay, fadeOut);
            client.inGameHud.setTitle(Text.literal(title));
            if (subtitle != null) {
                client.inGameHud.setSubtitle(Text.literal(subtitle));
            }
        }
    }

    /**
     * Clears any active title
     */
    public static void clearTitle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.clearTitle();
        }
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Shows success notification across all notification types
     */
    public static void showSuccess(String message) {
        sendChatMessage("✓ " + message, Formatting.GREEN);
        sendActionBar("✓ " + message, Formatting.GREEN);
        showToast("Success", message);
    }

    /**
     * Shows error notification across all notification types
     */
    public static void showError(String message) {
        sendChatMessage("✗ " + message, Formatting.RED);
        sendActionBar("✗ " + message, Formatting.RED);
        showAccessFailedToast("Error", message);
    }

    /**
     * Shows warning notification across all notification types
     */
    public static void showWarning(String message) {
        sendChatMessage("⚠ " + message, Formatting.YELLOW);
        sendActionBar("⚠ " + message, Formatting.YELLOW);
    }

    /**
     * Shows info notification across chat and action bar
     */
    public static void showInfo(String message) {
        sendChatMessage("ℹ " + message, Formatting.BLUE);
        sendActionBar("ℹ " + message, Formatting.BLUE);
    }

    /**
     * Shows a major announcement using title and chat
     */
    public static void showAnnouncement(String title, String subtitle, String chatMessage) {
        showTitle(title, subtitle, Formatting.GOLD, Formatting.YELLOW);
        sendChatMessage(chatMessage, Formatting.GOLD);
    }

    /**
     * Shows a critical alert using all notification types
     */
    public static void showCriticalAlert(String message) {
        showTitle("⚠ ALERT ⚠", message, Formatting.RED, Formatting.YELLOW);
        sendChatMessage("CRITICAL: " + message, Formatting.RED);
        sendActionBar("⚠ " + message, Formatting.RED);
        showAccessFailedToast("Critical Alert", message);
    }

    /**
     * Shows a subtle notification (action bar only)
     */
    public static void showSubtle(String message) {
        sendActionBar(message, Formatting.GRAY);
    }

    /**
     * Shows a celebration notification
     */
    public static void showCelebration(String message) {
        showTitle("🎉", message, Formatting.GOLD, Formatting.YELLOW);
        sendChatMessage("🎉 " + message, Formatting.GOLD);
        showToast("Celebration!", message);
    }
}