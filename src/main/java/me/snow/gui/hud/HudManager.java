package me.snow.gui.hud;

import me.snow.impl.Module;
import me.snow.impl.ModuleManager;
import me.snow.config.CracktonConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HudManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static HudManager instance;

    // Performance optimizations
    private static final long UPDATE_INTERVAL = 16; // 60fps
    private static final float ANIMATION_SMOOTHING = 0.15f;
    private static final float SLIDE_SPEED = 0.2f;
    private static final float FADE_SPEED = 0.08f;

    // Configuration
    private final HudConfig config = new HudConfig();

    // State management
    private final Map<Module, ModuleEntry> moduleEntryMap = new ConcurrentHashMap<>();
    private final List<ModuleEntry> sortedEntries = new ArrayList<>();
    private final List<ModuleEntry> renderQueue = new ArrayList<>(); // Cache for rendering
    private long lastUpdateTime = 0;
    private float headerSlideOffset = -200.0f;
    private boolean headerAnimating = true;
    private boolean needsResort = true;

    // Visual effects
    private final AnimationManager animationManager = new AnimationManager();
    private final ColorManager colorManager = new ColorManager();

    // Cached values for performance
    private int cachedMaxWidth = 0;
    private long lastWidthUpdate = 0;

    // Color utility methods (inlined for performance)
    private static int getAlpha(int color) { return (color >> 24) & 0xFF; }
    private static int getRed(int color) { return (color >> 16) & 0xFF; }
    private static int getGreen(int color) { return (color >> 8) & 0xFF; }
    private static int getBlue(int color) { return color & 0xFF; }
    private static int getArgb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    private HudManager() {
        // Private constructor for singleton
    }

    public static HudManager getInstance() {
        if (instance == null) {
            instance = new HudManager();
        }
        return instance;
    }

    public static void init() {
        getInstance();
    }

    public static void renderHud(DrawContext context) {
        if (instance != null && mc.world != null && mc.player != null && mc.currentScreen == null) {
            instance.render(context, 1.0f);
        }
    }

    public void render(DrawContext context, float tickDelta) {
        if (!config.arraylistEnabled || mc.options.hudHidden) {
            return;
        }

        updateModuleEntries();

        if (config.showHeader) {
            renderHeader(context, tickDelta);
        }

        renderArraylist(context, tickDelta);
    }

    private void renderHeader(DrawContext context, float tickDelta) {
        TextRenderer textRenderer = mc.textRenderer;
        String headerText = config.headerText;
        int textWidth = textRenderer.getWidth(headerText);
        int screenWidth = context.getScaledWindowWidth();

        // Smooth header animation
        animationManager.updateHeaderAnimation(tickDelta);

        // Calculate position
        int x = screenWidth - textWidth - config.padding;
        int y = Math.max(2, config.arraylistY - textRenderer.fontHeight - 3);
        x += (int) animationManager.getHeaderOffset();

        // Render header background with modern styling
        if (config.showBackground) {
            renderModernBackground(context, x - 3, y - 2, textWidth + 6,
                    textRenderer.fontHeight + 4, colorManager.getHeaderColor(), true);
        }

        // Header text with shadow and effects
        int color = colorManager.getHeaderColor();
        if (config.glowEffect) {
            renderGlowText(context, textRenderer, headerText, x, y, color);
        } else {
            context.drawTextWithShadow(textRenderer, headerText, x, y, color);
        }
    }

    private void renderArraylist(DrawContext context, float tickDelta) {
        if (renderQueue.isEmpty()) return;

        TextRenderer textRenderer = mc.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int yOffset = config.arraylistY;

        // Pre-calculate all module positions and widths for seamless borders
        List<ModuleRenderInfo> renderInfos = new ArrayList<>();

        for (int i = 0; i < renderQueue.size(); i++) {
            ModuleEntry entry = renderQueue.get(i);
            if (!entry.isVisible()) continue;

            String displayText = entry.getDisplayText();
            int textWidth = textRenderer.getWidth(displayText);
            int x = screenWidth - textWidth - config.padding + (int) entry.getSlideOffset();

            renderInfos.add(new ModuleRenderInfo(entry, displayText, x, yOffset, textWidth, i));
            yOffset += textRenderer.fontHeight + config.spacing;
        }

        // Render all backgrounds with seamless borders
        if (config.showBackground) {
            renderSeamlessBorders(context, renderInfos);
        }

        // Render all text with batch optimization
        renderModuleTexts(context, textRenderer, renderInfos);
    }

    private void renderModuleTexts(DrawContext context, TextRenderer textRenderer, List<ModuleRenderInfo> renderInfos) {
        for (ModuleRenderInfo info : renderInfos) {
            int textColor = colorManager.getModuleColor(info.index, info.entry);

            if (config.glowEffect) {
                renderGlowText(context, textRenderer, info.displayText, info.x, info.y, textColor);
            } else {
                context.drawTextWithShadow(textRenderer, info.displayText, info.x, info.y, textColor);
            }
        }
    }

    private void renderSeamlessBorders(DrawContext context, List<ModuleRenderInfo> renderInfos) {
        if (renderInfos.isEmpty()) return;

        TextRenderer textRenderer = mc.textRenderer;

        // Render backgrounds first
        for (ModuleRenderInfo info : renderInfos) {
            int bgColor = getArgb(
                    (int)(config.backgroundAlpha * info.entry.getFadeAlpha()),
                    getRed(config.backgroundColor),
                    getGreen(config.backgroundColor),
                    getBlue(config.backgroundColor)
            );

            context.fill(info.x - 2, info.y - 1, info.x + info.width + 2,
                    info.y + textRenderer.fontHeight + 1, bgColor);
        }

        // Render unified outline border
        renderUnifiedOutline(context, renderInfos, textRenderer);
    }

    private void renderUnifiedOutline(DrawContext context, List<ModuleRenderInfo> renderInfos, TextRenderer textRenderer) {
        int borderColor = colorManager.getBorderColor(renderInfos.get(0).entry);

        // Calculate the outline path for all modules
        List<OutlinePoint> outlinePoints = calculateOutlinePath(renderInfos, textRenderer);

        // Render the continuous outline
        renderContinuousOutline(context, outlinePoints, borderColor);

        // Connect to header if enabled
        if (config.showHeader) {
            renderHeaderConnection(context, renderInfos.get(0), borderColor);
        }
    }

    private List<OutlinePoint> calculateOutlinePath(List<ModuleRenderInfo> renderInfos, TextRenderer textRenderer) {
        List<OutlinePoint> points = new ArrayList<>();

        if (renderInfos.isEmpty()) return points;

        int fontHeight = textRenderer.fontHeight;

        // Start from top-left of first module
        ModuleRenderInfo first = renderInfos.get(0);
        int startX = first.x - 2;
        int startY = first.y - 1;

        // Top edge of first module
        points.add(new OutlinePoint(startX, startY, OutlineDirection.RIGHT));
        points.add(new OutlinePoint(first.x + first.width + 2, startY, OutlineDirection.DOWN));

        // Process each module's right edge and transitions
        for (int i = 0; i < renderInfos.size(); i++) {
            ModuleRenderInfo current = renderInfos.get(i);
            ModuleRenderInfo next = (i < renderInfos.size() - 1) ? renderInfos.get(i + 1) : null;

            int currentRight = current.x + current.width + 2;
            int currentBottom = current.y + fontHeight + 1;

            if (next != null) {
                int nextRight = next.x + next.width + 2;
                int nextTop = next.y - 1;
                int nextLeft = next.x - 2;

                // Right edge down to bottom of current
                points.add(new OutlinePoint(currentRight, currentBottom, OutlineDirection.RIGHT));

                if (currentRight <= nextRight) {
                    // Next module extends further or equal - go right then down
                    points.add(new OutlinePoint(nextRight, currentBottom, OutlineDirection.DOWN));
                    points.add(new OutlinePoint(nextRight, nextTop + fontHeight + 1, OutlineDirection.LEFT));
                } else {
                    // Current extends further - step down then continue
                    points.add(new OutlinePoint(currentRight, nextTop, OutlineDirection.LEFT));
                    points.add(new OutlinePoint(nextRight, nextTop, OutlineDirection.DOWN));
                    points.add(new OutlinePoint(nextRight, nextTop + fontHeight + 1, OutlineDirection.LEFT));
                }
            } else {
                // Last module - complete the bottom edge
                points.add(new OutlinePoint(currentRight, currentBottom, OutlineDirection.LEFT));
                points.add(new OutlinePoint(current.x - 2, currentBottom, OutlineDirection.UP));
            }
        }

        // Complete the path back to start (left edge up)
        ModuleRenderInfo last = renderInfos.get(renderInfos.size() - 1);
        int leftmostX = Integer.MAX_VALUE;
        for (ModuleRenderInfo info : renderInfos) {
            leftmostX = Math.min(leftmostX, info.x - 2);
        }

        // Left edge - trace back up with proper steps
        for (int i = renderInfos.size() - 1; i >= 0; i--) {
            ModuleRenderInfo current = renderInfos.get(i);
            ModuleRenderInfo prev = (i > 0) ? renderInfos.get(i - 1) : null;

            int currentLeft = current.x - 2;
            int currentTop = current.y - 1;

            if (prev != null) {
                int prevLeft = prev.x - 2;
                int prevBottom = prev.y + fontHeight + 1;

                if (currentLeft >= prevLeft) {
                    // Current is same or more indented
                    points.add(new OutlinePoint(currentLeft, currentTop, OutlineDirection.LEFT));
                    points.add(new OutlinePoint(prevLeft, currentTop, OutlineDirection.UP));
                } else {
                    // Current extends further left
                    points.add(new OutlinePoint(currentLeft, prevBottom, OutlineDirection.UP));
                }
            }
        }

        return points;
    }

    private void renderContinuousOutline(DrawContext context, List<OutlinePoint> points, int borderColor) {
        if (points.size() < 2) return;

        // Render each segment of the outline
        for (int i = 0; i < points.size(); i++) {
            OutlinePoint current = points.get(i);
            OutlinePoint next = points.get((i + 1) % points.size());

            // Draw line segment based on direction
            switch (current.direction) {
                case RIGHT:
                    if (next.x > current.x) {
                        context.fill(current.x, current.y, next.x, current.y + 1, borderColor);
                    }
                    break;
                case DOWN:
                    if (next.y > current.y) {
                        context.fill(current.x, current.y, current.x + 1, next.y, borderColor);
                    }
                    break;
                case LEFT:
                    if (next.x < current.x) {
                        context.fill(next.x, current.y, current.x, current.y + 1, borderColor);
                    }
                    break;
                case UP:
                    if (next.y < current.y) {
                        context.fill(current.x, next.y, current.x + 1, current.y, borderColor);
                    }
                    break;
            }
        }
    }

    // Helper classes for outline calculation
    private static class OutlinePoint {
        final int x, y;
        final OutlineDirection direction;

        OutlinePoint(int x, int y, OutlineDirection direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }
    }

    private enum OutlineDirection {
        UP, RIGHT, DOWN, LEFT
    }

    private void renderHeaderConnection(DrawContext context, ModuleRenderInfo firstModule, int borderColor) {
        TextRenderer textRenderer = mc.textRenderer;
        String headerText = config.headerText;
        int headerWidth = textRenderer.getWidth(headerText);
        int screenWidth = context.getScaledWindowWidth();

        int headerX = screenWidth - headerWidth - config.padding + (int) animationManager.getHeaderOffset();
        int headerY = Math.max(2, config.arraylistY - textRenderer.fontHeight - 3);
        int headerBottom = headerY + textRenderer.fontHeight + 2;
        int moduleTop = firstModule.y - 1;

        if (headerBottom < moduleTop) {
            int headerLeft = headerX - 3;
            int headerRight = headerX + headerWidth + 3;
            int moduleLeft = firstModule.x - 2;
            int moduleRight = firstModule.x + firstModule.width + 2;

            // Calculate unified bounds
            int leftBound = Math.min(headerLeft, moduleLeft);
            int rightBound = Math.max(headerRight, moduleRight);

            // Render connection efficiently
            context.fill(leftBound, headerBottom - 1, rightBound, headerBottom, borderColor);

            if (headerBottom < moduleTop) {
                context.fill(leftBound, headerBottom, leftBound + 1, moduleTop, borderColor);
                context.fill(rightBound - 1, headerBottom, rightBound, moduleTop, borderColor);
            }
        }
    }

    private void renderGlowText(DrawContext context, TextRenderer textRenderer,
                                String text, int x, int y, int color) {
        // Optimized glow effect
        int glowColor = getArgb(80, getRed(color), getGreen(color), getBlue(color));

        // Reduced glow passes for better performance
        int[] offsets = {-1, 0, 1};
        for (int dx : offsets) {
            for (int dy : offsets) {
                if (dx == 0 && dy == 0) continue;
                context.drawText(textRenderer, text, x + dx, y + dy, glowColor, false);
            }
        }

        // Main text
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    private void renderModernBackground(DrawContext context, int x, int y, int width, int height,
                                        int color, boolean isHeader) {
        // Modern background with subtle styling
        int bgColor = getArgb(config.backgroundAlpha, getRed(config.backgroundColor),
                getGreen(config.backgroundColor), getBlue(config.backgroundColor));

        context.fill(x, y, x + width, y + height, bgColor);

        // Efficient border rendering
        context.fill(x - 1, y - 1, x, y + height + 1, color); // Left
        context.fill(x + width, y - 1, x + width + 1, y + height + 1, color); // Right
        if (isHeader) {
            context.fill(x - 1, y - 1, x + width + 1, y, color); // Top
        }
    }

    private void updateModuleEntries() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) return;
        lastUpdateTime = currentTime;

        List<Module> enabledModules = ModuleManager.getEnabledModules();

        // Optimized module management
        Set<Module> currentModules = new HashSet<>(enabledModules);

        // Remove disabled modules
        moduleEntryMap.entrySet().removeIf(entry -> {
            if (!currentModules.contains(entry.getKey())) {
                entry.getValue().startFadeOut();
                if (entry.getValue().shouldRemove()) {
                    needsResort = true;
                    return true;
                }
            }
            return false;
        });

        // Add new modules
        for (Module module : enabledModules) {
            if (moduleEntryMap.putIfAbsent(module, new ModuleEntry(module)) == null) {
                needsResort = true;
            }
        }

        // Update all entries
        boolean anyUpdated = false;
        for (ModuleEntry entry : moduleEntryMap.values()) {
            if (entry.update()) {
                anyUpdated = true;
            }
        }

        // Update sorted list only when necessary
        if (needsResort || anyUpdated) {
            updateSortedEntries();
            needsResort = false;
        }
    }

    private void updateSortedEntries() {
        renderQueue.clear();
        renderQueue.addAll(moduleEntryMap.values());

        if (config.sortByLength) {
            renderQueue.sort(Comparator.comparingInt((ModuleEntry entry) ->
                    mc.textRenderer.getWidth(entry.getDisplayText())).reversed());
        } else {
            renderQueue.sort(Comparator.comparing(entry -> entry.module.getName()));
        }
    }

    private int getMaxModuleWidth() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWidthUpdate > 500) { // Update every 500ms
            cachedMaxWidth = renderQueue.stream()
                    .mapToInt(entry -> mc.textRenderer.getWidth(entry.getDisplayText()))
                    .max()
                    .orElse(0);
            lastWidthUpdate = currentTime;
        }
        return cachedMaxWidth;
    }

    // Helper class for render information
    private static class ModuleRenderInfo {
        final ModuleEntry entry;
        final String displayText;
        final int x, y, width, index;

        ModuleRenderInfo(ModuleEntry entry, String displayText, int x, int y, int width, int index) {
            this.entry = entry;
            this.displayText = displayText;
            this.x = x;
            this.y = y;
            this.width = width;
            this.index = index;
        }
    }

    // Enhanced ModuleEntry class with better performance
    private static class ModuleEntry {
        private final Module module;
        private float slideOffset = -200.0f;
        private boolean isSliding = true;
        private boolean fadingOut = false;
        private float fadeAlpha = 1.0f;
        private int stableFrames = 0;
        private String cachedDisplayText;
        private boolean textDirty = true;

        public ModuleEntry(Module module) {
            this.module = module;
        }

        public boolean update() {
            boolean changed = false;

            if (fadingOut) {
                float oldAlpha = fadeAlpha;
                fadeAlpha = Math.max(0, fadeAlpha - FADE_SPEED);
                return oldAlpha != fadeAlpha;
            }

            if (isSliding) {
                float oldOffset = slideOffset;
                slideOffset += (0 - slideOffset) * SLIDE_SPEED;

                if (Math.abs(slideOffset) < 0.5f) {
                    stableFrames++;
                    if (stableFrames > 3) {
                        slideOffset = 0.0f;
                        isSliding = false;
                        changed = true;
                    }
                } else {
                    stableFrames = 0;
                }

                if (oldOffset != slideOffset) {
                    changed = true;
                }
            }

            // Update cached display text if needed
            if (textDirty) {
                cachedDisplayText = module.getName();
                textDirty = false;
                changed = true;
            }

            return changed;
        }

        public void startFadeOut() {
            fadingOut = true;
        }

        public boolean shouldRemove() {
            return fadingOut && fadeAlpha <= 0;
        }

        public boolean isVisible() {
            return fadeAlpha > 0.01f;
        }

        public String getDisplayText() {
            if (cachedDisplayText == null) {
                cachedDisplayText = module.getName();
            }
            return cachedDisplayText;
        }

        public float getSlideOffset() {
            return slideOffset;
        }

        public float getFadeAlpha() {
            return fadeAlpha;
        }

        public Module getModule() {
            return module;
        }

        public void markTextDirty() {
            textDirty = true;
        }
    }

    // Animation management with better performance
    private class AnimationManager {
        private float targetHeaderOffset = 0.0f;

        public void updateHeaderAnimation(float tickDelta) {
            if (headerAnimating) {
                float oldOffset = headerSlideOffset;
                headerSlideOffset += (targetHeaderOffset - headerSlideOffset) * ANIMATION_SMOOTHING * tickDelta;

                if (Math.abs(headerSlideOffset - targetHeaderOffset) < 1.0f) {
                    headerSlideOffset = targetHeaderOffset;
                    headerAnimating = false;
                }
            }
        }

        public float getHeaderOffset() {
            return headerSlideOffset;
        }

        public void setHeaderTarget(float target) {
            if (Math.abs(targetHeaderOffset - target) > 0.1f) {
                targetHeaderOffset = target;
                headerAnimating = true;
            }
        }
    }

    // Optimized color management
    private class ColorManager {
        private int[] rainbowCache = new int[64]; // Cache rainbow colors
        private long lastRainbowUpdate = 0;
        private static final long RAINBOW_UPDATE_INTERVAL = 50; // Update every 50ms

        public int getHeaderColor() {
            if (config.rainbow) {
                return getRainbowColor(0);
            }
            return config.headerColor;
        }

        public int getModuleColor(int index, ModuleEntry entry) {
            int baseColor;

            if (config.rainbow) {
                baseColor = getRainbowColor(index);
            } else if (config.chroma) {
                baseColor = getChromaColor(index);
            } else {
                baseColor = config.textColor;
            }

            return applyFade(baseColor, entry.getFadeAlpha());
        }

        public int getBorderColor(ModuleEntry entry) {
            int baseColor;

            if (config.chroma) {
                baseColor = getChromaColor(0);
            } else if (config.rainbow) {
                baseColor = getRainbowColor(0);
            } else {
                baseColor = config.headerColor;
            }

            return applyFade(baseColor, entry.getFadeAlpha());
        }

        private int getRainbowColor(int index) {
            long time = System.currentTimeMillis();

            // Update rainbow cache periodically
            if (time - lastRainbowUpdate > RAINBOW_UPDATE_INTERVAL) {
                updateRainbowCache(time);
                lastRainbowUpdate = time;
            }

            // Use cached value if available
            int cacheIndex = index % rainbowCache.length;
            if (rainbowCache[cacheIndex] != 0) {
                return rainbowCache[cacheIndex];
            }

            // Fallback calculation
            float hue = ((time * config.rainbowSpeed * 0.001f + index * 0.1f) % 360) / 360.0f;
            return Color.HSBtoRGB(hue, config.rainbowSaturation, config.rainbowBrightness) | 0xFF000000;
        }

        private void updateRainbowCache(long time) {
            for (int i = 0; i < rainbowCache.length; i++) {
                float hue = ((time * config.rainbowSpeed * 0.001f + i * 0.1f) % 360) / 360.0f;
                rainbowCache[i] = Color.HSBtoRGB(hue, config.rainbowSaturation, config.rainbowBrightness) | 0xFF000000;
            }
        }

        private int getChromaColor(int index) {
            long time = System.currentTimeMillis();
            float hue = ((time * 0.003f + index * 0.05f) % 360) / 360.0f;
            return Color.HSBtoRGB(hue, 1.0f, 1.0f) | 0xFF000000;
        }

        private int applyFade(int color, float alpha) {
            int a = (int)(getAlpha(color) * alpha);
            return getArgb(a, getRed(color), getGreen(color), getBlue(color));
        }
    }

    // Configuration class with validation
    public static class HudConfig {
        // Basic settings
        public boolean arraylistEnabled = true;
        public int padding = 4;
        public int arraylistY = 18;
        public int spacing = 1;

        // Visual settings
        public boolean showBackground = true;
        public boolean showHeader = true;
        public boolean sortByLength = true;
        public boolean modernBorders = true;
        public boolean glowEffect = false;

        // Color settings
        public boolean rainbow = false;
        public boolean chroma = true;
        public int backgroundColor = 0x80000000;
        public int textColor = 0xFFFFFFFF;
        public int headerColor = 0xFF00FF00;
        public int backgroundAlpha = 128;

        // Animation settings
        public int rainbowSpeed = 2;
        public float rainbowSaturation = 1.0f;
        public float rainbowBrightness = 1.0f;

        // Text settings
        public String headerText = "Crackton V2";

        // Validation methods
        public void setPadding(int padding) {
            this.padding = Math.max(0, Math.min(padding, 20));
        }

        public void setArraylistY(int y) {
            this.arraylistY = Math.max(0, y);
        }

        public void setBackgroundAlpha(int alpha) {
            this.backgroundAlpha = Math.max(0, Math.min(alpha, 255));
        }
    }

    // Getters for configuration
    public HudConfig getConfig() {
        return config;
    }

    // Legacy getters for compatibility with optimized setters
    public boolean isArraylistEnabled() { return config.arraylistEnabled; }
    public void setArraylistEnabled(boolean enabled) {
        if (config.arraylistEnabled != enabled) {
            config.arraylistEnabled = enabled;
            needsResort = true;
        }
    }

    public int getArraylistY() { return config.arraylistY; }
    public void setArraylistY(int y) {
        int newY = Math.max(0, y);
        if (config.arraylistY != newY) {
            config.arraylistY = newY;
        }
    }

    public boolean isSortByLength() { return config.sortByLength; }
    public void setSortByLength(boolean sortByLength) {
        if (config.sortByLength != sortByLength) {
            config.sortByLength = sortByLength;
            needsResort = true;
        }
    }

    public boolean isShowBackground() { return config.showBackground; }
    public void setShowBackground(boolean showBackground) {
        config.showBackground = showBackground;
    }

    public boolean isShowHeader() { return config.showHeader; }
    public void setShowHeader(boolean showHeader) {
        config.showHeader = showHeader;
    }

    // Additional utility methods
    public void invalidateCache() {
        needsResort = true;
        cachedMaxWidth = 0;
        lastWidthUpdate = 0;
    }

    public void resetAnimations() {
        headerSlideOffset = -200.0f;
        headerAnimating = true;

        for (ModuleEntry entry : moduleEntryMap.values()) {
            entry.slideOffset = -200.0f;
            entry.isSliding = true;
            entry.stableFrames = 0;
        }
    }
}