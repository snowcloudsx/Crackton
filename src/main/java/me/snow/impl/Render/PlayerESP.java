package me.snow.impl.Render;

import me.snow.impl.Category;
import me.snow.impl.Module;
import me.snow.impl.settings.BooleanSetting;
import me.snow.impl.settings.NumberSetting;
import me.snow.impl.settings.ModeSetting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import java.util.OptionalDouble;
import java.awt.Color;

public class PlayerESP extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // Settings - declare without final so they can be properly initialized
    private ModeSetting espMode;
    private BooleanSetting filledMode;
    private ColorSetting espColor;
    private BooleanSetting throughWalls;
    private NumberSetting lineWidth;
    private BooleanSetting drawHealthBar;
    private BooleanSetting drawName;
    private NumberSetting espRange;
    private ModeSetting colorMode;

    // Custom render layers
    private static final RenderLayer ESP_LINES = RenderLayer.of(
            "esp_lines",
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

    private static final RenderLayer ESP_FILLED = RenderLayer.of(
            "esp_filled",
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

    private static final RenderLayer ESP_NO_DEPTH = RenderLayer.of(
            "esp_no_depth",
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

    public PlayerESP() {
        super("PlayerESP", "Shows the location of players with multiple modes", Category.RENDER);

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!this.isEnabled() || mc.player == null || mc.world == null) return;

            MatrixStack matrixStack = context.matrixStack();
            Camera camera = context.camera();
            Vec3d camPos = camera.getPos();

            VertexConsumerProvider.Immediate buffers = mc.getBufferBuilders().getEntityVertexConsumers();

            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player || player.isInvisible()) continue;

                // Check range
                double distance = mc.player.distanceTo(player);
                if (distance > espRange.getValue()) continue;

                Box box = player.getBoundingBox().offset(-camPos.x, -camPos.y, -camPos.z);

                // Get color based on color mode
                Color color = getESPColor(player, distance);
                float r = color.getRed() / 255f;
                float g = color.getGreen() / 255f;
                float b = color.getBlue() / 255f;
                float a = color.getAlpha() / 255f;

                // Render based on selected mode
                String currentMode = espMode.getValue();
                if (currentMode.equals("Box")) {
                    renderBox(matrixStack, buffers, box, r, g, b, a);
                } else if (currentMode.equals("Glow")) {
                    renderGlow(matrixStack, buffers, box, r, g, b, a);
                } else if (currentMode.equals("Outline")) {
                    renderOutline(matrixStack, buffers, box, r, g, b, a);
                } else if (currentMode.equals("Chams")) {
                    renderChams(matrixStack, buffers, box, r, g, b, a);
                } else if (currentMode.equals("Wireframe")) {
                    renderWireframe(matrixStack, buffers, box, r, g, b, a);
                } else if (currentMode.equals("Corners")) {
                    renderCorners(matrixStack, buffers, box, r, g, b, a);
                }

                // Optional health bar
                if (drawHealthBar.getValue()) {
                    renderHealthBar(matrixStack, buffers, box, player);
                }
            }

            buffers.draw();
        });
    }

    @Override
    protected void initSettings() {
        // Properly initialize settings using the Module's add methods
        espMode = addModeSetting("ESP Mode", "Box", "Box", "Glow", "Outline", "Chams", "Wireframe", "Corners");
        filledMode = addBooleanSetting("Filled", "Fill the ESP boxes", false);
        espColor = new ColorSetting("Color", "ESP Color", new Color(0, 255, 0, 255)); // Custom setting
        throughWalls = addBooleanSetting("Through Walls", "Render ESP through walls", true);
        lineWidth = addNumberSetting("Line Width", "Width of ESP lines", 2.0, 0.5, 5.0);
        drawHealthBar = addBooleanSetting("Health Bar", "Draw player health bars", false);
        drawName = addBooleanSetting("Name Tags", "Draw player names", false);
        espRange = addNumberSetting("Range", "ESP render range", 100, 10, 500);
        colorMode = addModeSetting("Color Mode", "Static", "Static", "Rainbow", "Distance", "Health");
    }

    private void renderBox(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, float r, float g, float b, float a) {
        if (filledMode.getValue()) {
            VertexConsumer filledConsumer = buffers.getBuffer(ESP_FILLED);
            drawFilledBox(matrices, filledConsumer, box, r, g, b, a * 0.3f);
        }

        VertexConsumer lineConsumer = buffers.getBuffer(throughWalls.getValue() ? ESP_NO_DEPTH : ESP_LINES);
        drawBox(matrices, lineConsumer, box, r, g, b, a);
    }

    private void renderGlow(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, float r, float g, float b, float a) {
        // Render multiple boxes with increasing size and decreasing alpha for glow effect
        for (int i = 0; i < 3; i++) {
            float expand = i * 0.05f;
            Box expandedBox = box.expand(expand, expand, expand);
            float glowAlpha = a * (0.8f - i * 0.2f);

            VertexConsumer consumer = buffers.getBuffer(ESP_FILLED);
            drawFilledBox(matrices, consumer, expandedBox, r, g, b, glowAlpha * 0.2f);
        }

        // Draw outline
        VertexConsumer lineConsumer = buffers.getBuffer(ESP_LINES);
        drawBox(matrices, lineConsumer, box, r, g, b, a);
    }

    private void renderOutline(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, float r, float g, float b, float a) {
        // Thicker outline effect
        VertexConsumer consumer = buffers.getBuffer(ESP_LINES);

        // Draw multiple offset lines for thickness
        for (float offset = -0.01f; offset <= 0.01f; offset += 0.005f) {
            Box offsetBox = box.offset(offset, 0, 0);
            drawBox(matrices, consumer, offsetBox, r, g, b, a * 0.7f);
        }
    }

    private void renderChams(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, float r, float g, float b, float a) {
        // Render filled box with high alpha for "chams" effect
        VertexConsumer filledConsumer = buffers.getBuffer(ESP_FILLED);
        drawFilledBox(matrices, filledConsumer, box, r, g, b, a * 0.6f);

        // Add subtle outline
        VertexConsumer lineConsumer = buffers.getBuffer(ESP_LINES);
        drawBox(matrices, lineConsumer, box, r * 0.8f, g * 0.8f, b * 0.8f, a * 0.8f);
    }

    private void renderWireframe(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, float r, float g, float b, float a) {
        VertexConsumer consumer = buffers.getBuffer(ESP_LINES);

        // Draw more detailed wireframe with diagonal lines
        drawBox(matrices, consumer, box, r, g, b, a);

        // Add diagonal lines for wireframe effect
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        // Diagonal lines
        addLine(consumer, positionMatrix, x1, y1, z1, x2, y2, z2, r, g, b, a * 0.5f);
        addLine(consumer, positionMatrix, x2, y1, z1, x1, y2, z2, r, g, b, a * 0.5f);
        addLine(consumer, positionMatrix, x1, y1, z2, x2, y2, z1, r, g, b, a * 0.5f);
        addLine(consumer, positionMatrix, x2, y1, z2, x1, y2, z1, r, g, b, a * 0.5f);
    }

    private void renderCorners(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, float r, float g, float b, float a) {
        VertexConsumer consumer = buffers.getBuffer(ESP_LINES);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;
        float cornerSize = 0.2f;

        // Bottom corners
        // Corner 1 (x1, y1, z1)
        addLine(consumer, positionMatrix, x1, y1, z1, x1 + cornerSize, y1, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y1, z1, x1, y1 + cornerSize, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y1, z1, x1, y1, z1 + cornerSize, r, g, b, a);

        // Corner 2 (x2, y1, z1)
        addLine(consumer, positionMatrix, x2, y1, z1, x2 - cornerSize, y1, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z1, x2, y1 + cornerSize, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z1, x2, y1, z1 + cornerSize, r, g, b, a);

        // Corner 3 (x2, y1, z2)
        addLine(consumer, positionMatrix, x2, y1, z2, x2 - cornerSize, y1, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z2, x2, y1 + cornerSize, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y1, z2, x2, y1, z2 - cornerSize, r, g, b, a);

        // Corner 4 (x1, y1, z2)
        addLine(consumer, positionMatrix, x1, y1, z2, x1 + cornerSize, y1, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y1, z2, x1, y1 + cornerSize, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y1, z2, x1, y1, z2 - cornerSize, r, g, b, a);

        // Top corners
        // Corner 5 (x1, y2, z1)
        addLine(consumer, positionMatrix, x1, y2, z1, x1 + cornerSize, y2, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y2, z1, x1, y2 - cornerSize, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y2, z1, x1, y2, z1 + cornerSize, r, g, b, a);

        // Corner 6 (x2, y2, z1)
        addLine(consumer, positionMatrix, x2, y2, z1, x2 - cornerSize, y2, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y2, z1, x2, y2 - cornerSize, z1, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y2, z1, x2, y2, z1 + cornerSize, r, g, b, a);

        // Corner 7 (x2, y2, z2)
        addLine(consumer, positionMatrix, x2, y2, z2, x2 - cornerSize, y2, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y2, z2, x2, y2 - cornerSize, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x2, y2, z2, x2, y2, z2 - cornerSize, r, g, b, a);

        // Corner 8 (x1, y2, z2)
        addLine(consumer, positionMatrix, x1, y2, z2, x1 + cornerSize, y2, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y2, z2, x1, y2 - cornerSize, z2, r, g, b, a);
        addLine(consumer, positionMatrix, x1, y2, z2, x1, y2, z2 - cornerSize, r, g, b, a);
    }

    private void renderHealthBar(MatrixStack matrices, VertexConsumerProvider.Immediate buffers, Box box, PlayerEntity player) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercentage = health / maxHealth;

        // Health bar dimensions
        float barWidth = (float) (box.maxX - box.minX);
        float barHeight = 0.1f;
        float barY = (float) box.maxY + 0.2f;

        // Health bar background (red)
        VertexConsumer consumer = buffers.getBuffer(ESP_FILLED);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        // Background bar (red)
        addQuad(consumer, positionMatrix,
                (float) box.minX, barY, (float) box.minZ,
                (float) box.maxX, barY, (float) box.minZ,
                (float) box.maxX, barY + barHeight, (float) box.minZ,
                (float) box.minX, barY + barHeight, (float) box.minZ,
                1.0f, 0.0f, 0.0f, 0.8f);

        // Health bar (green)
        float healthWidth = barWidth * healthPercentage;
        addQuad(consumer, positionMatrix,
                (float) box.minX, barY, (float) box.minZ,
                (float) box.minX + healthWidth, barY, (float) box.minZ,
                (float) box.minX + healthWidth, barY + barHeight, (float) box.minZ,
                (float) box.minX, barY + barHeight, (float) box.minZ,
                0.0f, 1.0f, 0.0f, 0.8f);
    }

    private Color getESPColor(PlayerEntity player, double distance) {
        String colorMode = this.colorMode.getValue();

        switch (colorMode) {
            case "Static":
                return espColor.getValue();

            case "Rainbow":
                long time = System.currentTimeMillis();
                float hue = (time % 2000L) / 2000.0f;
                return Color.getHSBColor(hue, 1.0f, 1.0f);

            case "Distance":
                Double maxDistance = espRange.getValue();
                float distanceRatio = (float) Math.min(distance / maxDistance, 1.0);
                // Green for close, red for far
                return new Color(distanceRatio, 1.0f - distanceRatio, 0.0f, 1.0f);

            case "Health":
                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();
                float healthRatio = health / maxHealth;
                // Red for low health, green for high health
                return new Color(1.0f - healthRatio, healthRatio, 0.0f, 1.0f);

            default:
                return espColor.getValue();
        }
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {}

    // Drawing helper methods
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

    // Simple ColorSetting class (basic implementation) - keeping for ESP color
    public static class ColorSetting {
        private final String name;
        private final String description;
        private Color value;

        public ColorSetting(String name, String description, Color defaultValue) {
            this.name = name;
            this.description = description;
            this.value = defaultValue;
        }

        public Color getValue() {
            return value;
        }

        public void setValue(Color value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}