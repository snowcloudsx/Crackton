package me.snow.gui.hud;

import me.snow.impl.Module;
import me.snow.impl.ModuleManager;
import me.snow.config.CracktonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HudManager {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static HudManager instance;

    // Arraylist settings
    private boolean arraylistEnabled = true;
    private int arraylistX = 2;
    private int arraylistY = 2;
    private boolean sortByLength = true;
    private boolean showBackground = true;
    private boolean rainbow = false;
    private int backgroundColor = 0x80000000; // Semi-transparent black
    private int textColor = 0xFFFFFFFF; // White
    private int rainbowSpeed = 2;
    private float rainbowSaturation = 0.7f;
    private float rainbowBrightness = 1.0f;

    // Animation
    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 50; // Update every 50ms

    public HudManager() {
        instance = this;
    }

    public static HudManager getInstance() {
        if (instance == null) {
            instance = new HudManager();
        }
        return instance;
    }

    /**
     * Initialize the HUD manager - call this in your main loop/client init
     */
    public static void init() {
        if (instance == null) {
            instance = new HudManager();
        }
    }

    /**
     * Main render method to be called from your main loop
     */
    public static void renderHud() {
        if (instance != null && mc.world != null && mc.player != null) {
            // Debug: Check if we have enabled modules
            if (ModuleManager.getEnabledModules().isEmpty()) {
                System.out.println("HudManager: No enabled modules to display");
                return;
            }

            // Get current render context - this may need adjustment based on your setup
            if (mc.currentScreen == null) { // Only render when not in a screen
                System.out.println("HudManager: Rendering arraylist with " + ModuleManager.getEnabledModules().size() + " modules");
                // Create a simple render call - you'll need to hook this into your actual render event
                instance.renderArraylistOnly();
            }
        } else {
            System.out.println("HudManager: Cannot render - missing instance, world, or player");
        }
    }

    /**
     * Render the arraylist HUD (legacy method for when you have DrawContext)
     */
    public void render(DrawContext context, float tickDelta) {
        if (!arraylistEnabled || mc.options.hudHidden) {
            return;
        }

        updateModuleEntries();
        renderArraylist(context, tickDelta);
    }

    /**
     * Render the arraylist with DrawContext
     */
    private void renderArraylist(DrawContext context, float tickDelta) {
        if (moduleEntries.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int yOffset = arraylistY;

        for (int i = 0; i < moduleEntries.size(); i++) {
            ModuleEntry entry = moduleEntries.get(i);
            String displayText = entry.getDisplayText();
            int textWidth = textRenderer.getWidth(displayText);

            // Flip x to render from right
            int x = screenWidth - textWidth - 5;

            // Slide offset direction reversed
            float slideOffset = entry.getSlideOffset();
            x -= (int) slideOffset;

            int y = yOffset;

            if (showBackground) {
                context.fill(x - 2, y - 1, x + textWidth + 2, y + textRenderer.fontHeight + 1, backgroundColor);
            }

            int color = getRainbowColor(i);

            context.drawText(textRenderer, displayText, x, y, color, true);

            yOffset += textRenderer.fontHeight + 2;
        }
    }



    /**
     * Update the module entries list
     */
    private void updateModuleEntries() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        List<Module> enabledModules = ModuleManager.getEnabledModules();

        // Remove entries for disabled modules
        moduleEntries.removeIf(entry -> !enabledModules.contains(entry.module));

        // Add entries for new enabled modules
        for (Module module : enabledModules) {
            boolean found = moduleEntries.stream().anyMatch(entry -> entry.module == module);
            if (!found) {
                moduleEntries.add(new ModuleEntry(module));
            }
        }

        // Sort modules
        if (sortByLength) {
            moduleEntries.sort(Comparator.comparingInt((ModuleEntry entry) ->
                    mc.textRenderer.getWidth(entry.getDisplayText())).reversed());
        } else {
            moduleEntries.sort(Comparator.comparing(entry -> entry.module.getName()));
        }

        // Update animations
        for (ModuleEntry entry : moduleEntries) {
            entry.update();
        }
    }

    /**
     * Render arraylist without external context (for main loop calls)
     */
    private void renderArraylistOnly() {
        if (!arraylistEnabled || mc.options.hudHidden) {
            return;
        }

        updateModuleEntries();

        if (moduleEntries.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        int yOffset = arraylistY;

        // Use the game's current render matrices
        MatrixStack matrices = new MatrixStack();

        for (int i = 0; i < moduleEntries.size(); i++) {
            ModuleEntry entry = moduleEntries.get(i);
            String displayText = entry.getDisplayText();
            int textWidth = textRenderer.getWidth(displayText);

            // Calculate position
            int x = arraylistX;
            int y = yOffset;

            // Apply slide animation
            float slideOffset = entry.getSlideOffset();
            x += (int) slideOffset;

            // Text color
            int color = textColor;
            if (rainbow) {
                color = getRainbowColor(i);
            }

            // Use the newer draw method for 1.21.4
            matrices.push();
            matrices.translate(0, 0, 300); // Bring to front

            // Draw text (shadow parameter is boolean)
            textRenderer.draw(displayText, x, y, color, true, matrices.peek().getPositionMatrix(),
                    mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);

            matrices.pop();

            // Move to next line
            yOffset += textRenderer.fontHeight + 2;
        }

        // Flush the rendering
        mc.getBufferBuilders().getEntityVertexConsumers().draw();
    }

    /**
     * Simple test render to verify it's working
     */
    public static void testRender() {
        if (mc.world != null && mc.player != null && mc.currentScreen == null) {
            MatrixStack matrices = new MatrixStack();
            matrices.push();
            matrices.translate(0, 0, 300);
            mc.textRenderer.draw("TEST HUD", 10, 10, 0xFFFFFFFF, true, matrices.peek().getPositionMatrix(),
                    mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            matrices.pop();
            mc.getBufferBuilders().getEntityVertexConsumers().draw();
        }
    }

    /**
     * Get rainbow color for arraylist
     */
    private int getRainbowColor(int index) {
        long time = System.currentTimeMillis();
        float slowerRainbowSpeed = rainbowSpeed * 0.5f; // Half the original speed
        float hue = ((time * slowerRainbowSpeed + index * 50) % 3600) / 3600.0f;
        int rgb = java.awt.Color.HSBtoRGB(hue, rainbowSaturation, rainbowBrightness);
        return 0xFF000000 | rgb; // Add alpha channel
    }

    /**
     * Module entry class for animation and display
     */
    private static class ModuleEntry {
        private final Module module;
        private float slideOffset = -100.0f; // Start off-screen
        private boolean isSliding = true;
        private long creationTime;

        public ModuleEntry(Module module) {
            this.module = module;
            this.creationTime = System.currentTimeMillis();
        }

        public void update() {
            if (isSliding) {
                // Smooth slide-in animation
                slideOffset += (0 - slideOffset) * 0.15f;

                // Stop sliding when close enough
                if (Math.abs(slideOffset) < 1.0f) {
                    slideOffset = 0.0f;
                    isSliding = false;
                }
            }
        }

        public String getDisplayText() {
            return module.getName();
        }

        public float getSlideOffset() {
            return slideOffset;
        }
    }

    // Getters and setters for configuration
    public boolean isArraylistEnabled() {
        return arraylistEnabled;
    }

    public void setArraylistEnabled(boolean enabled) {
        this.arraylistEnabled = enabled;
    }

    public int getArraylistX() {
        return arraylistX;
    }

    public void setArraylistX(int x) {
        this.arraylistX = x;
    }

    public int getArraylistY() {
        return arraylistY;
    }

    public void setArraylistY(int y) {
        this.arraylistY = y;
    }

    public boolean isSortByLength() {
        return sortByLength;
    }

    public void setSortByLength(boolean sortByLength) {
        this.sortByLength = sortByLength;
    }

    public boolean isShowBackground() {
        return showBackground;
    }

    public void setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(int rainbowSpeed) {
        this.rainbowSpeed = rainbowSpeed;
    }

    public float getRainbowSaturation() {
        return rainbowSaturation;
    }

    public void setRainbowSaturation(float rainbowSaturation) {
        this.rainbowSaturation = rainbowSaturation;
    }

    public float getRainbowBrightness() {
        return rainbowBrightness;
    }

    public void setRainbowBrightness(float rainbowBrightness) {
        this.rainbowBrightness = rainbowBrightness;
    }
}