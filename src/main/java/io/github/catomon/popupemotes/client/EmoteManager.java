package io.github.catomon.popupemotes.client;

import io.github.catomon.popupemotes.Config;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class EmoteManager {
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        if (!Config.useCompatRender) return;

        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new EmoteLayerRenderer(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Config.useCompatRender) {
            EmoteLayerRenderer.tickEmotes();
        } else {
            EmoteRenderer.onClientTick(event);
        }
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Post event) {
        if (Config.useCompatRender) {
            //
        } else {
            EmoteRenderer.onPlayerRender(event);
        }
    }
}