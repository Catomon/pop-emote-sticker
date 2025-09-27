package io.github.catomon.popupemotes.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmoteLayerRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final int TICKS_FADE_IN = 5;
    private static final int TICKS_STATIC = 40;
    private static final int TICKS_FADE_OUT = 5;
    private static final int TOTAL_TICKS = TICKS_FADE_IN + TICKS_STATIC + TICKS_FADE_OUT;

    private static final float BASE_SIZE = 0.65f;

    private static final Map<UUID, EmoteData> activeEmotes = new HashMap<>();
    private static final Map<UUID, ResourceLocation> emoteTextureLocations = new HashMap<>();

    public EmoteLayerRenderer(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    public static void showEmoteOnPlayer(UUID playerUUID, int emoteId) {
        Map<Integer, byte[]> emotes;
        UUID localUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;

        if (localUUID != null && localUUID.equals(playerUUID)) {
            emotes = ClientEmotePacksManager.getLocalEmotePack();
        } else {
            emotes = ClientEmotePacksManager.getPlayerEmotePack(playerUUID);
        }

        if (emotes == null) {
            emotes = new HashMap<>();
        }

        byte[] imageData = emotes.get(emoteId);

        if (imageData == null || imageData.length == 0) {
            Map<Integer, byte[]> defaultEmotes = ClientEmotePacksManager.loadDefaultEmotesAsBytes();
            if (defaultEmotes.containsKey(emoteId)) {
                imageData = defaultEmotes.get(emoteId);
            } else {
                // No emote found, abort rendering
                return;
            }
        }

        NativeImage nativeImage;
        try {
            nativeImage = NativeImage.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

        EmoteData existing = activeEmotes.put(playerUUID, new EmoteData(emoteId, dynamicTexture));
        if (existing != null) {
            existing.close();
        }

        ResourceLocation textureLocation = Minecraft.getInstance().getTextureManager()
                .register("emote_" + playerUUID, dynamicTexture);
        emoteTextureLocations.put(playerUUID, textureLocation);
    }

    public static void tickEmotes() {
        var iterator = activeEmotes.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            EmoteData emoteData = entry.getValue();
            emoteData.ageTicks++;
            if (emoteData.ageTicks >= TOTAL_TICKS) {
                emoteData.close();
                UUID playerUUID = entry.getKey();
                iterator.remove();
                emoteTextureLocations.remove(playerUUID);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        UUID playerUUID = player.getUUID();

        if (!activeEmotes.containsKey(playerUUID))
            return;

        EmoteData emoteData = activeEmotes.get(playerUUID);
        ResourceLocation emoteResource = emoteTextureLocations.get(playerUUID);
        if (emoteResource == null) return;

        float alpha;
        float scale;

        if (emoteData.ageTicks < TICKS_FADE_IN) {
            float progress = emoteData.ageTicks / (float) TICKS_FADE_IN;
            alpha = progress;
            scale = 1.0f + (float) Math.sin(progress * Math.PI * 4) * 0.3f;
        } else if (emoteData.ageTicks < TICKS_FADE_IN + TICKS_STATIC) {
            alpha = 1f;
            scale = 1f;
        } else if (emoteData.ageTicks < TOTAL_TICKS) {
            float progress = (emoteData.ageTicks - TICKS_FADE_IN - TICKS_STATIC) / (float) TICKS_FADE_OUT;
            alpha = 1f - progress;
            scale = 1f - progress;
        } else {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0, -player.getBbHeight() + 0.5, 0);

        float halfSize = BASE_SIZE / 2f;

        float cameraYaw = Minecraft.getInstance().getEntityRenderDispatcher().camera.getYRot();
        float cameraPitch = Minecraft.getInstance().getEntityRenderDispatcher().camera.getXRot();

        poseStack.mulPose(Axis.YP.rotationDegrees(cameraYaw - player.yBodyRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraPitch));

        poseStack.scale(scale, scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindForSetup(emoteResource);

        VertexConsumer vertexBuilder = bufferSource.getBuffer(RenderType.entityTranslucent(emoteResource));

        vertexBuilder.vertex(poseStack.last().pose(), -halfSize, halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0f, 0f, 1f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), -halfSize, -halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0f, 0f, 1f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), halfSize, -halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0f, 0f, 1f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), halfSize, halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0f, 0f, 1f)
                .endVertex();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static class EmoteData {
        final int emoteId;
        final DynamicTexture texture;
        int ageTicks = 0;

        EmoteData(int emoteId, DynamicTexture texture) {
            this.emoteId = emoteId;
            this.texture = texture;
        }

        void close() {
            texture.close();
        }
    }
}