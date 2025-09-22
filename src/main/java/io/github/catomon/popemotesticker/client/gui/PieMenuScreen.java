package io.github.catomon.popemotesticker.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import io.github.catomon.popemotesticker.PopEmoteSticker;
import io.github.catomon.popemotesticker.client.EmoteClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PieMenuScreen extends Screen {
    private static final int SLICE_COUNT = 8;
    private static final float RADIUS = 100f;

    public static int selectedSlice = -1;
    private static final ResourceLocation SLICE_TEXTURE = ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/gui/pie_slice.png");

    private final Map<Integer, DynamicTexture> dynamicEmoteTextures = new HashMap<>();
    private final Map<Integer, ResourceLocation> dynamicEmoteResourceLocations = new HashMap<>();

    public PieMenuScreen() {
        super(Component.literal("Choose Emote"));
        loadDynamicTextures();
    }

    private void loadDynamicTextures() {
        Map<Integer, byte[]> localEmotes = EmoteClientManager.getLocalEmotePack();
        var textureManager = Minecraft.getInstance().getTextureManager();

        for (int i = 0; i < SLICE_COUNT; i++) {
            byte[] imageBytes = localEmotes.get(i);
            if (imageBytes != null) {
                try {
                    NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(imageBytes));
                    DynamicTexture texture = new DynamicTexture(nativeImage);
                    dynamicEmoteTextures.put(i, texture);
                    ResourceLocation location = textureManager.register("emote_dynamic_" + i, texture);
                    dynamicEmoteResourceLocations.put(i, location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        onClose();
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        updateSelection((float) mouseX, (float) mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        updateSelection((float) mouseX, (float) mouseY);
        return true;
    }

    private void updateSelection(float mouseX, float mouseY) {
        float centerX = this.width / 2f;
        float centerY = this.height / 2f;

        float dx = mouseX - centerX;
        float dy = mouseY - centerY;

        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > RADIUS || distance < 20) {
            selectedSlice = -1;
            return;
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + (360.0 / SLICE_COUNT / 2);
        if (angle < 0) angle += 360;

        double sliceAngle = 360.0 / SLICE_COUNT;
        selectedSlice = (int) (angle / sliceAngle);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        float centerX = this.width / 2f;
        float centerY = this.height / 2f;

        guiGraphics.pose().pushPose();
        float sliceAngle = 360f / SLICE_COUNT;

        // Draw pie slices
        for (int i = 0; i < SLICE_COUNT; i++) {
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(centerX, centerY, 0);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(i * sliceAngle));
            guiGraphics.pose().translate(RADIUS / 2, 0, 0);

            float scale = (i == selectedSlice) ? 1f : .8f;
            guiGraphics.pose().scale(scale, scale, 1);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-i * sliceAngle));

            RenderSystem.setShaderTexture(0, SLICE_TEXTURE);
            guiGraphics.blit(SLICE_TEXTURE, -32, -32, 0, 0, 64, 64, 64, 64);

            guiGraphics.pose().popPose();
        }

        // Draw emotes above slices using dynamic textures or static fallback
        for (int i = 0; i < SLICE_COUNT; i++) {
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(centerX, centerY, 0);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(i * sliceAngle));
            guiGraphics.pose().translate(RADIUS / 2, 0, 5f);

            float scale = (i == selectedSlice) ? 1.2f : 1.0f;
            guiGraphics.pose().scale(scale, scale, 1);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-i * sliceAngle));

            ResourceLocation textureLocation = dynamicEmoteResourceLocations.get(i);
            if (textureLocation != null) {
                RenderSystem.setShaderTexture(0, textureLocation);
                guiGraphics.blit(textureLocation, -16, -16, 0, 0, 32, 32, 32, 32);
            } else if (i < EmoteClientManager.EMOTE_TEXTURES.length) {
                RenderSystem.setShaderTexture(0, EmoteClientManager.EMOTE_TEXTURES[i]);
                guiGraphics.blit(EmoteClientManager.EMOTE_TEXTURES[i], -16, -16, 0, 0, 32, 32, 32, 32);
            }

            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        String instructions = "Move mouse to select emote, release the button to confirm";
        guiGraphics.drawString(
                this.font,
                instructions,
                (int) (centerX - this.font.width(instructions) / 2f),
                (int) (centerY + RADIUS + 10),
                0xFFFFFF
        );
    }

    @Override
    public void onClose() {
        super.onClose();
        dynamicEmoteTextures.values().forEach(DynamicTexture::close);
        dynamicEmoteTextures.clear();
        dynamicEmoteResourceLocations.clear();
    }
}