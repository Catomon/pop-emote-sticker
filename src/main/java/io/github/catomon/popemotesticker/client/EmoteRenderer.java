package io.github.catomon.popemotesticker.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.catomon.popemotesticker.PopEmoteSticker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EmoteRenderer {
    private static final int TICKS_FADE_IN = 5;
    private static final int TICKS_STATIC = 40;
    private static final int TICKS_FADE_OUT = 5;
    private static final int TOTAL_TICKS = TICKS_FADE_IN + TICKS_STATIC + TICKS_FADE_OUT;

    private static final float BASE_SIZE = 0.65f;

    private static final Map<UUID, EmoteData> activeEmotes = new HashMap<>();

    public static void showEmoteOnPlayer(UUID playerUUID, int emoteId) {
        Map<Integer, byte[]> emotes;
        UUID localUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;

        if (localUUID != null && localUUID.equals(playerUUID)) {
            emotes = EmoteClientManager.getLocalEmotePack();
        } else {
            emotes = EmoteClientManager.getPlayerEmotePack(playerUUID);
        }

        byte[] imageData = emotes.get(emoteId);

        if (imageData == null || imageData.length == 0) {
            Map<Integer, byte[]> defaultEmotes = EmoteClientManager.loadDefaultEmotesAsBytes();
            if (defaultEmotes.containsKey(emoteId)) {
                imageData = defaultEmotes.get(emoteId);
            } else {
                // No emote texture available, abort
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

        EmoteData previous = activeEmotes.put(playerUUID, new EmoteData(emoteId, dynamicTexture));
        if (previous != null) {
            previous.close();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            var iterator = activeEmotes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, EmoteData> entry = iterator.next();
                EmoteData emoteData = entry.getValue();
                emoteData.ageTicks++;
                if (emoteData.ageTicks >= TOTAL_TICKS) {
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();

        if (!activeEmotes.containsKey(playerUUID)) return;

        EmoteData data = activeEmotes.get(playerUUID);
        DynamicTexture texture = data.texture;

        ResourceLocation textureLocation = Minecraft.getInstance().getTextureManager().register("emote_" + playerUUID, texture);

        PoseStack poseStack = event.getPoseStack();

        float alpha;
        float scale;
        if (data.ageTicks < TICKS_FADE_IN) {
            float progress = data.ageTicks / (float) TICKS_FADE_IN;
            alpha = progress;
            scale = 1.0f + (float) Math.sin(progress * Math.PI * 4) * 0.3f;
        } else if (data.ageTicks < TICKS_FADE_IN + TICKS_STATIC) {
            alpha = 1f;
            scale = 1f;
        } else if (data.ageTicks < TOTAL_TICKS) {
            float progress = (data.ageTicks - TICKS_FADE_IN - TICKS_STATIC) / (float) TICKS_FADE_OUT;
            alpha = 1f - progress;
            scale = 1f - progress;
        } else {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0, player.getBbHeight() + 0.5, 0);
        var camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        mc.getTextureManager().bindForSetup(textureLocation);

        VertexConsumer vertexBuilder = mc.renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(textureLocation));

        float halfSize = BASE_SIZE / 2f;

        vertexBuilder.vertex(poseStack.last().pose(), -halfSize,  halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(),  halfSize,  halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(),  halfSize, -halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), -halfSize, -halfSize, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        mc.renderBuffers().bufferSource().endBatch(RenderType.entityTranslucent(textureLocation));

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static class EmoteData implements AutoCloseable {
        final int emoteId;
        final DynamicTexture texture;
        int ageTicks = 0;

        EmoteData(int emoteId, DynamicTexture texture) {
            this.emoteId = emoteId;
            this.texture = texture;
        }

        @Override
        public void close() {
            texture.close();
        }
    }
}