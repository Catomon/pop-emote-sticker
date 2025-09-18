package io.github.catomon.popemotesticker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.catomon.popemotesticker.PopEmoteStickerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EmoteRenderer {
    public static final ResourceLocation[] EMOTE_TEXTURES = new ResourceLocation[]{
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote1.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote2.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote3.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote4.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote5.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote6.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote7.png"),
            new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote8.png")
    };

    private static final int TICKS_FADE_IN = 5;
    private static final int TICKS_STATIC = 40;
    private static final int TICKS_FADE_OUT = 5;
    private static final int TOTAL_TICKS = TICKS_FADE_IN + TICKS_STATIC + TICKS_FADE_OUT;

    private static final float BASE_SIZE = 0.65f;

    private static final Map<UUID, EmoteData> activeEmotes = new HashMap<>();

    private static class EmoteData {
        final int emoteId;
        int ageTicks = 0;

        EmoteData(int emoteId) {
            this.emoteId = emoteId;
        }
    }

    public static void showEmoteOnPlayer(UUID playerUUID, int emoteId) {
        activeEmotes.put(playerUUID, new EmoteData(emoteId));
    }

    // Increment ageTicks once per game tick
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

        if (!activeEmotes.containsKey(playerUUID))
            return;

        EmoteData emoteData = activeEmotes.get(playerUUID);
        ResourceLocation emoteTexture = EMOTE_TEXTURES[emoteData.emoteId];

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
            // This case is now handled in tick event, but kept for safety
            activeEmotes.remove(playerUUID);
            return;
        }

        poseStack.pushPose();

        // Above player head
        poseStack.translate(0, player.getBbHeight() + 0.5, 0);

        // Face camera
        var camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        poseStack.mulPose(camera.rotation());

        // Scale and flip vertically for correct orientation
        poseStack.scale(-scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        mc.getTextureManager().bindForSetup(emoteTexture);
        VertexConsumer vertexBuilder = mc.renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(emoteTexture));

        float size = BASE_SIZE / 2f;

        vertexBuilder.vertex(poseStack.last().pose(), -size, size, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), size, size, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), size, -size, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        vertexBuilder.vertex(poseStack.last().pose(), -size, -size, 0f)
                .color(1f, 1f, 1f, alpha)
                .uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0f, 1f, 0f)
                .endVertex();

        mc.renderBuffers().bufferSource().endBatch(RenderType.entityTranslucent(emoteTexture));

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}