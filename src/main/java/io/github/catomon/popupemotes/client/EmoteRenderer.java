package io.github.catomon.popupemotes.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT)
public class EmoteRenderer {
    private static final int TICKS_FADE_IN = 5;
    private static final int TICKS_STATIC = 40;
    private static final int TICKS_FADE_OUT = 5;
    private static final int TOTAL_TICKS = TICKS_FADE_IN + TICKS_STATIC + TICKS_FADE_OUT;

    private static final float BASE_SIZE = 0.65f;

    // Track active emotes on players with age for fade logic
    private static final Map<UUID, EmoteData> activeEmotes = new HashMap<>();

    private static class EmoteData {
        final int emoteId;
        final DynamicTexture texture; // DynamicTexture created from byte[] data
        int ageTicks = 0;

        EmoteData(int emoteId, DynamicTexture texture) {
            this.emoteId = emoteId;
            this.texture = texture;
        }

        void close() {
            texture.close(); // Free texture resource when no longer needed
        }
    }

    public static void showEmoteOnPlayer(UUID playerUUID, int emoteId) {
        Map<Integer, byte[]> emotes;
        UUID localUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;

        if (localUUID != null && localUUID.equals(playerUUID)) {
            // Use local player emote pack (with default fallbacks merged)
            emotes = EmoteClientManager.getLocalEmotePack();
        } else {
            // Use cached network emote pack or empty map
            emotes = EmoteClientManager.getPlayerEmotePack(playerUUID);
        }

        byte[] imageData = emotes.get(emoteId);

        // If emote data missing in player pack, fallback to default cached emotes
        if (imageData == null || imageData.length == 0) {
            Map<Integer, byte[]> defaultEmotes = EmoteClientManager.loadDefaultEmotesAsBytes();
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

        DynamicTexture texture = new DynamicTexture(nativeImage);

        EmoteData existing = activeEmotes.put(playerUUID, new EmoteData(emoteId, texture));
        if (existing != null) {
            existing.close();
        }
    }

    // Tick updates fade timing and removes old emotes
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var iterator = activeEmotes.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            EmoteData emoteData = entry.getValue();
            emoteData.ageTicks++;
            if (emoteData.ageTicks >= TOTAL_TICKS) {
                emoteData.close();
                iterator.remove();
            }
        }
    }

    // Render emote above players with fade-in/out and scaling animations
    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();

        if (!activeEmotes.containsKey(playerUUID))
            return;

        EmoteData emoteData = activeEmotes.get(playerUUID);
        DynamicTexture texture = emoteData.texture;
        ResourceLocation emoteResource = Minecraft.getInstance().getTextureManager().register("emote_" + playerUUID, texture);

        PoseStack poseStack = event.getPoseStack();

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

        poseStack.translate(0, player.getBbHeight() + 0.5, 0);

        var camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        poseStack.mulPose(camera.rotation());

        poseStack.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        mc.getTextureManager().bindForSetup(emoteResource);
        VertexConsumer vertexBuilder = mc.renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(emoteResource));

        float size = BASE_SIZE / 2f;

        vertexBuilder.addVertex(poseStack.last().pose(), -size, size, 0f)
                .setColor(1f, 1f, 1f, alpha)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);

        vertexBuilder.addVertex(poseStack.last().pose(), size, size, 0f)
                .setColor(1f, 1f, 1f, alpha)
                .setUv(1f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);

        vertexBuilder.addVertex(poseStack.last().pose(), size, -size, 0f)
                .setColor(1f, 1f, 1f, alpha)
                .setUv(1f, 1f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);

        vertexBuilder.addVertex(poseStack.last().pose(), -size, -size, 0f)
                .setColor(1f, 1f, 1f, alpha)
                .setUv(0f, 1f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);

        mc.renderBuffers().bufferSource().endBatch(RenderType.entityTranslucent(emoteResource));

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}