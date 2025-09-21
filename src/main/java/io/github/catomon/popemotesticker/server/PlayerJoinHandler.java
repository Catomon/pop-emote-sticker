package io.github.catomon.popemotesticker.server;

import io.github.catomon.popemotesticker.network.stc.RequestEmotePackPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class PlayerJoinHandler {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("PlayerJoinHandler");
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PacketDistributor.sendToPlayer(player, new RequestEmotePackPayload());
    }
}