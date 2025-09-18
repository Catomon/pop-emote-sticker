package io.github.catomon.popemotesticker.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.catomon.popemotesticker.PopEmoteStickerMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static io.github.catomon.popemotesticker.client.EmoteRenderer.EMOTE_TEXTURES;

public class PieMenuScreen extends Screen {
    private static final int SLICE_COUNT = 8;
    private static final float RADIUS = 100f;  // Increased radius for wider spread

    public static int selectedSlice = -1;

    private static final ResourceLocation SLICE_TEXTURE = new ResourceLocation(PopEmoteStickerMod.MODID, "textures/gui/pie_slice.png");

    public PieMenuScreen() {
        super(Component.literal("Choose Emote"));
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return true;
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
        this.renderBackground(guiGraphics);

        float centerX = this.width / 2f;
        float centerY = this.height / 2f;

        guiGraphics.pose().pushPose();

        float sliceAngle = 360f / SLICE_COUNT;

        // Draw pie slices smaller and spread wider
        for (int i = 0; i < SLICE_COUNT; i++) {
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(centerX, centerY, 0);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(i * sliceAngle));
            guiGraphics.pose().translate(RADIUS / 2, 0, 0);

            float scale = (i == selectedSlice) ? 1f : .8f;
            guiGraphics.pose().scale(scale, scale, 1);

            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-i * sliceAngle));

            // Draw slice (64x64 pixels)
            RenderSystem.setShaderTexture(0, SLICE_TEXTURE);
            guiGraphics.blit(SLICE_TEXTURE, -32, -32, 0, 0, 64, 64, 64, 64);

            guiGraphics.pose().popPose();
        }

        // Draw emotes upright and on top of slices
        for (int i = 0; i < SLICE_COUNT; i++) {
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(centerX, centerY, 0);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(i * sliceAngle));
            guiGraphics.pose().translate(RADIUS / 2, 0, 5f); // Slight Z offset to render above slices

            float scale = (i == selectedSlice) ? 1.2f : 1.0f;
            guiGraphics.pose().scale(scale, scale, 1);

            // Cancel rotation so emote appears upright
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-i * sliceAngle));

            if (i < EMOTE_TEXTURES.length) {
                RenderSystem.setShaderTexture(0, EMOTE_TEXTURES[i]);
                guiGraphics.blit(EMOTE_TEXTURES[i], -16, -16, 0, 0, 32, 32, 32, 32);
            }

            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        // Draw instructions below the pie menu
        String instructions = "Move mouse to select emote, release the button to confirm";
        guiGraphics.drawString(
                this.font,
                instructions,
                (int) (centerX - this.font.width(instructions) / 2f),
                (int) (centerY + RADIUS + 10),
                0xFFFFFF
        );

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}