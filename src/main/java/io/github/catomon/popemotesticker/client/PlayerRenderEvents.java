package io.github.catomon.popemotesticker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.catomon.popemotesticker.PopEmoteStickerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

//@Mod.EventBusSubscriber(modid = PopEmoteStickerMod.MODID, value = Dist.CLIENT)
//public class PlayerRenderEvents {
//
//    private static final ResourceLocation TEXTURE = new ResourceLocation(PopEmoteStickerMod.MODID, "textures/emotes/emote.png");
//
//    @SubscribeEvent
//    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
//        Player player = event.getEntity();
//        PoseStack poseStack = event.getPoseStack();
//        MultiBufferSource bufferSource = event.getMultiBufferSource();
//        int packedLight = event.getPackedLight();
//
//        poseStack.pushPose();
//
//        // Move above player head
//        poseStack.translate(0.0, player.getBbHeight() + 0.5, 0.0);
//
//        // Face camera
//        float cameraYaw = Minecraft.getInstance().getEntityRenderDispatcher().camera.getYRot();
//        float cameraYawInRadians = (float) Math.toRadians(cameraYaw);
//        poseStack.mulPose(new Quaternionf().rotationYXZ(0, -cameraYawInRadians, 0));
//
//        // Scale billboard
//        float scale = 0.5f;
//        poseStack.scale(scale, scale, scale);
//
//        VertexConsumer vertexBuilder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
//        RenderSystem.setShaderTexture(0, TEXTURE);
//        RenderSystem.enableBlend();
//
//        vertexBuilder.vertex(poseStack.last().pose(), -0.5f, 0.5f, 0f)
//                .color(255, 255, 255, 255)
//                .uv(0f, 0f)
//                .overlayCoords(0, 10)
//                .uv2(packedLight)
//                .normal(0f, 1f, 0f)
//                .endVertex();
//
//        vertexBuilder.vertex(poseStack.last().pose(), 0.5f, 0.5f, 0f)
//                .color(255, 255, 255, 255)
//                .uv(1f, 0f)
//                .overlayCoords(0, 10)
//                .uv2(packedLight)
//                .normal(0f, 1f, 0f)
//                .endVertex();
//
//        vertexBuilder.vertex(poseStack.last().pose(), 0.5f, -0.5f, 0f)
//                .color(255, 255, 255, 255)
//                .uv(1f, 1f)
//                .overlayCoords(0, 10)
//                .uv2(packedLight)
//                .normal(0f, 1f, 0f)
//                .endVertex();
//
//        vertexBuilder.vertex(poseStack.last().pose(), -0.5f, -0.5f, 0f)
//                .color(255, 255, 255, 255)
//                .uv(0f, 1f)
//                .overlayCoords(0, 10)
//                .uv2(packedLight)
//                .normal(0f, 1f, 0f)
//                .endVertex();
//
//        RenderSystem.disableBlend();
//
//        poseStack.popPose();
//    }
//}