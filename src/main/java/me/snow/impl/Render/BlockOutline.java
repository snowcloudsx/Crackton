package me.snow.impl.Render;

import me.snow.impl.Category;
import me.snow.impl.Module;
import me.snow.impl.settings.Setting;
import me.snow.impl.settings.BooleanSetting;
import me.snow.impl.settings.NumberSetting;
import me.snow.impl.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.BlockView;
import org.joml.Matrix4f;
import java.util.OptionalDouble;

import java.awt.*;

public class BlockOutline extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    // Custom render layers for block outline
    private static final RenderLayer BLOCK_OUTLINE_LINES = RenderLayer.of(
            "block_outline_lines",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.DEBUG_LINES,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.POSITION_COLOR_PROGRAM)
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2.0)))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .build(false)
    );

    private static final RenderLayer BLOCK_OUTLINE_FILLED = RenderLayer.of(
            "block_outline_filled",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.POSITION_COLOR_PROGRAM)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .build(false)
    );


    // Settings
    private BooleanSetting customOutline;
    private BooleanSetting glowEffect;
    private BooleanSetting fillBlock;
    private NumberSetting lineWidth;
    private NumberSetting opacity;
    private NumberSetting fillOpacity;
    private ModeSetting colorMode;

    // Static color settings
    private NumberSetting redValue;
    private NumberSetting greenValue;
    private NumberSetting blueValue;

    // Animation settings
    private BooleanSetting animate;
    private NumberSetting animationSpeed;

    // Hover settings THIS WONT WORK FOR SHIT no this module

    private BooleanSetting hoverEffect;
    private NumberSetting hoverScale;
    private BooleanSetting hoverPulse;

    private long animationTime = 0;
    private float pulsePhase = 0.0f;

    public BlockOutline() {
        super("BlockOutline", "Customize block outline and hover effects", Category.RENDER);
    }

    @Override
    protected void initSettings() {
        // Main settings
        customOutline = addBooleanSetting("Custom Outline", "Enable custom block outline rendering", true);
        glowEffect = addBooleanSetting("Glow Effect", "Add glow effect around outlined blocks", false);
        fillBlock = addBooleanSetting("Fill Block", "Fill the inside of outlined blocks", false);
        lineWidth = addNumberSetting("Line Width", "Width of the outline lines", 2.0f, 0.1f, 10.0f);
        opacity = addNumberSetting("Opacity", "Transparency of the outline", 255, 0, 255);
        fillOpacity = addNumberSetting("Fill Opacity", "Transparency of the block fill", 50, 0, 255);
        colorMode = addModeSetting("Color Mode", "Static", "Static", "Rainbow", "Distance", "Block Type");

        // Color settings (visible when Static mode is selected)
        redValue = addNumberSetting("Red", "Red color component", 255, 0, 255);
        greenValue = addNumberSetting("Green", "Green color component", 255, 0, 255);
        blueValue = addNumberSetting("Blue", "Blue color component", 255, 0, 255);

        // Animation settings
        animate = addBooleanSetting("Animate", "Enable color animations", false);
        animationSpeed = addNumberSetting("Animation Speed", "Speed of color animations", 1.0f, 0.1f, 5.0f);

        // Hover settings
        hoverEffect = addBooleanSetting("Hover Effect", "Enable special effects when hovering over blocks", true);
        hoverScale = addNumberSetting("Hover Scale", "Scale factor for hovered blocks", 1.05f, 1.0f, 1.5f);
        hoverPulse = addBooleanSetting("Hover Pulse", "Enable pulsing effect on hovered blocks", false);
    }

    @Override
    public void onEnable() {
        animationTime = System.currentTimeMillis();
        pulsePhase = 0.0f;
    }

    @Override
    public void onDisable() {
        // Reset any persistent effects
        animationTime = 0;
        pulsePhase = 0.0f;
    }

    @Override
    public void onTick() {
        if (!this.isEnabled()) return;

        // Update animation timers
        if (animate.getValue()) {
            animationTime = System.currentTimeMillis();
        }

        if (hoverPulse.getValue()) {
            pulsePhase += 0.1f * animationSpeed.getValue();
            if (pulsePhase > Math.PI * 2) {
                pulsePhase = 0.0f;
            }
        }
    }

    // This method would be called from a mixin in WorldRenderer
    public void renderBlockOutline(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                   BlockPos blockPos, Box box, double cameraX, double cameraY, double cameraZ) {
        if (!this.isEnabled() || !customOutline.getValue()) return;

        // Calculate color based on mode
        Color color = calculateColor(blockPos);

        // Apply hover effects
        Box renderBox = box;
        if (hoverEffect.getValue() && isBlockHovered(blockPos)) {
            renderBox = applyHoverEffects(box);

            // Modify color for hover if needed
            if (hoverPulse.getValue()) {
                float pulseFactor = (float)(Math.sin(pulsePhase) * 0.3f + 0.7f);
                color = new Color(
                        (int)(color.getRed() * pulseFactor),
                        (int)(color.getGreen() * pulseFactor),
                        (int)(color.getBlue() * pulseFactor),
                        color.getAlpha()
                );
            }
        }

        // Render fill if enabled
        if (fillBlock.getValue()) {
            renderBlockFill(matrices, vertexConsumers, renderBox, color, cameraX, cameraY, cameraZ);
        }

        // Render outline
        renderBlockOutlineCustom(matrices, vertexConsumers, renderBox, color, cameraX, cameraY, cameraZ);

        // Add glow effect if enabled
        if (glowEffect.getValue()) {
            renderGlowEffect(matrices, vertexConsumers, renderBox, color, cameraX, cameraY, cameraZ);
        }
    }

    private Color calculateColor(BlockPos blockPos) {
        float alpha = (float) (opacity.getValue() / 255.0f);

        switch (colorMode.getValue()) {
            case "Rainbow":
                return getRainbowColor(alpha);
            case "Distance":
                return getDistanceColor(blockPos, alpha);
            case "Block Type":
                return getBlockTypeColor(blockPos, alpha);
            default: // Static
                return new Color(
                        redValue.getValue().intValue(),
                        greenValue.getValue().intValue(),
                        blueValue.getValue().intValue(),
                        (int)(alpha * 255)
                );
        }
    }

    private Color getRainbowColor(float alpha) {
        if (!animate.getValue()) {
            return new Color(255, 0, 0, (int)(alpha * 255));
        }

        float time = (float) ((System.currentTimeMillis() % 3000) / 3000.0f * animationSpeed.getValue());
        float hue = time % 1.0f;
        java.awt.Color rainbow = java.awt.Color.getHSBColor(hue, 1.0f, 1.0f);
        return new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), (int)(alpha * 255));
    }

    private Color getDistanceColor(BlockPos blockPos, float alpha) {
        if (mc.player == null) return new Color(255, 255, 255, (int)(alpha * 255));

        double distance = mc.player.getPos().distanceTo(Vec3d.ofCenter(blockPos));
        float factor = Math.min(1.0f, (float)(distance / 10.0)); // Max distance of 10 blocks

        // Red close, blue far
        int red = (int)(255 * (1.0f - factor));
        int blue = (int)(255 * factor);
        return new Color(red, 0, blue, (int)(alpha * 255));
    }

    private Color getBlockTypeColor(BlockPos blockPos, float alpha) {
        if (mc.world == null) return new Color(255, 255, 255, (int)(alpha * 255));

        // Get block hardness and map to color - Updated for 1.21.4
        float hardness;
        try {
            hardness = mc.world.getBlockState(blockPos).getHardness(mc.world, blockPos);
        } catch (Exception e) {
            // Fallback for blocks that don't support hardness check
            hardness = 1.0f;
        }

        if (hardness < 0) { // Unbreakable
            return new Color(255, 0, 255, (int)(alpha * 255)); // Magenta
        } else if (hardness == 0) { // Instant break
            return new Color(0, 255, 0, (int)(alpha * 255)); // Green
        } else if (hardness < 1.0f) { // Soft
            return new Color(255, 255, 0, (int)(alpha * 255)); // Yellow
        } else if (hardness < 3.0f) { // Medium
            return new Color(255, 165, 0, (int)(alpha * 255)); // Orange
        } else { // Hard
            return new Color(255, 0, 0, (int)(alpha * 255)); // Red
        }
    }

    private boolean isBlockHovered(BlockPos blockPos) {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
        return blockHit.getBlockPos().equals(blockPos);
    }

    private Box applyHoverEffects(Box box) {
        float scale = hoverScale.getValue().floatValue();

        if (hoverPulse.getValue()) {
            float pulseFactor = (float)(Math.sin(pulsePhase) * 0.05f + 1.0f);
            scale *= pulseFactor;
        }

        if (scale == 1.0f) return box;

        Vec3d center = box.getCenter();
        Vec3d size = new Vec3d(box.getLengthX(), box.getLengthY(), box.getLengthZ()).multiply(scale * 0.5);

        return new Box(
                center.x - size.x, center.y - size.y, center.z - size.z,
                center.x + size.x, center.y + size.y, center.z + size.z
        );
    }

    private void renderBlockFill(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                 Box box, Color color, double cameraX, double cameraY, double cameraZ) {
        float alpha = (float) (fillOpacity.getValue() / 255.0f);

        VertexConsumer consumer = vertexConsumers.getBuffer(BLOCK_OUTLINE_FILLED);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = alpha;

        // Offset box by camera position
        Box offsetBox = new Box(
                box.minX - cameraX, box.minY - cameraY, box.minZ - cameraZ,
                box.maxX - cameraX, box.maxY - cameraY, box.maxZ - cameraZ
        );

        drawFilledBox(matrices, consumer, offsetBox, r, g, b, a);
    }

    private void renderBlockOutlineCustom(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                          Box box, Color color, double cameraX, double cameraY, double cameraZ) {
        VertexConsumer consumer = vertexConsumers.getBuffer(BLOCK_OUTLINE_LINES);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        // Offset box by camera position
        Box offsetBox = new Box(
                box.minX - cameraX, box.minY - cameraY, box.minZ - cameraZ,
                box.maxX - cameraX, box.maxY - cameraY, box.maxZ - cameraZ
        );

        drawBox(matrices, consumer, offsetBox, r, g, b, a);
    }

    private void renderGlowEffect(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                  Box box, Color color, double cameraX, double cameraY, double cameraZ) {
        // Create a slightly larger box for glow effect
        Box glowBox = box.expand(0.05);
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 30);

        renderBlockFill(matrices, vertexConsumers, glowBox, glowColor, cameraX, cameraY, cameraZ);
    }

    // Getter methods for settings (to be used by mixins)
    public boolean shouldRenderCustomOutline() {
        return this.isEnabled() && customOutline.getValue();
    }

    public float getLineWidth() {
        return lineWidth.getValue().floatValue();
    }

    // Custom drawing methods based on PlayerESP
    private void drawBox(MatrixStack matrices, VertexConsumer consumer, Box box, float r, float g, float b, float a) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // Bottom face edges
        addLine(consumer, positionMatrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y1, z2, x1, y1, z1, r, g, b, a);

        // Top face edges
        addLine(consumer, positionMatrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y2, z2, x1, y2, z1, r, g, b, a);

        // Vertical edges
        addLine(consumer, positionMatrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private void addLine(VertexConsumer consumer, Matrix4f positionMatrix,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        consumer.vertex(positionMatrix, x1, y1, z1).color(r, g, b, a);
        consumer.vertex(positionMatrix, x2, y2, z2).color(r, g, b, a);
    }

    private void drawFilledBox(MatrixStack matrices, VertexConsumer consumer, Box box, float r, float g, float b, float a) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // Bottom face
        addQuad(consumer, positionMatrix, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a);
        // Top face
        addQuad(consumer, positionMatrix, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, b, a);
        // Front face
        addQuad(consumer, positionMatrix, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, r, g, b, a);
        // Back face
        addQuad(consumer, positionMatrix, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, r, g, b, a);
        // Left face
        addQuad(consumer, positionMatrix, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, b, a);
        // Right face
        addQuad(consumer, positionMatrix, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r, g, b, a);
    }

    private void addQuad(VertexConsumer consumer, Matrix4f positionMatrix,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float r, float g, float b, float a) {
        consumer.vertex(positionMatrix, x1, y1, z1).color(r, g, b, a);
        consumer.vertex(positionMatrix, x2, y2, z2).color(r, g, b, a);
        consumer.vertex(positionMatrix, x3, y3, z3).color(r, g, b, a);
        consumer.vertex(positionMatrix, x4, y4, z4).color(r, g, b, a);
    }
}