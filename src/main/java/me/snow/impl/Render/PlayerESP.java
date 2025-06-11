package me.snow.impl.Render;

import me.snow.impl.Category;
import me.snow.impl.Module;
import me.snow.impl.settings.BooleanSetting;
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

    // Settings
    private me.snow.impl.settings.BooleanSetting filledMode;
    private ColorSetting espColor;

    // Custom render layer for lines
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

    // Custom render layer for filled boxes
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

    public PlayerESP() {
        super("PlayerESP", "Shows the location of players", Category.RENDER);

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!this.isEnabled() || mc.player == null || mc.world == null) return;

            MatrixStack matrixStack = context.matrixStack();
            Camera camera = context.camera();
            Vec3d camPos = camera.getPos();

            VertexConsumerProvider.Immediate buffers = mc.getBufferBuilders().getEntityVertexConsumers();

            // Get color from settings
            Color color = espColor.getValue();
            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;
            float a = color.getAlpha() / 255f;

            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player || player.isInvisible()) continue;

                Box box = player.getBoundingBox().offset(-camPos.x, -camPos.y, -camPos.z);

                if (filledMode.getValue()) {
                    // Draw filled box with transparency
                    VertexConsumer filledConsumer = buffers.getBuffer(ESP_FILLED);
                    drawFilledBox(matrixStack, filledConsumer, box, r, g, b, a * 0.3f);
                }

                // Always draw outline
                VertexConsumer lineConsumer = buffers.getBuffer(ESP_LINES);
                drawBox(matrixStack, lineConsumer, box, r, g, b, a);
            }

            buffers.draw();
        });
    }

    @Override
    protected void initSettings() {
        filledMode = addBooleanSetting("Filled", "Filled Box", false);
        espColor = addColorSetting("Color", "ESP Color", new Color(0, 255, 0, 255)); // Default green
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {}

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
        // Since we're using POSITION_COLOR format, we don't need normals
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

    // Simple ColorSetting class (basic implementation)
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

    // Simple BooleanSetting class (basic implementation)
    public static class BooleanSetting {
        private final String name;
        private final String description;
        private boolean value;

        public BooleanSetting(String name, String description, boolean defaultValue) {
            this.name = name;
            this.description = description;
            this.value = defaultValue;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }


    protected ColorSetting addColorSetting(String name, String description, Color defaultValue) {
        return new ColorSetting(name, description, defaultValue);
    }
}