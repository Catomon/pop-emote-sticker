package io.github.catomon.popupemotes.client;

import io.github.catomon.popupemotes.Config;
import io.github.catomon.popupemotes.PopUpEmotes;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

@Mod.EventBusSubscriber(modid = PopUpEmotes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class ClientModEventHandlers {
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        if (!Config.useCompatRender) return;

        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            System.out.println(renderer);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new EmoteLayerRenderer(playerRenderer));
            }
        }
    }
}

@Mod.EventBusSubscriber(modid = PopUpEmotes.MODID, value = Dist.CLIENT)
public class EmoteManager {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (Config.useCompatRender) {
                EmoteLayerRenderer.tickEmotes();
            } else {
                EmoteRenderer.onClientTick(event);
            }
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