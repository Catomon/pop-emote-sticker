package io.github.catomon.popupemotes.server;

import io.github.catomon.popupemotes.network.stc.AllPlayersEmotePacksPayload;
import io.github.catomon.popupemotes.network.stc.RequestEmotePackPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class PlayerJoinHandler {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        PacketDistributor.sendToPlayer(player, new AllPlayersEmotePacksPayload(ServerEmoteManager.getAllPlayerEmotePacks()));

        PacketDistributor.sendToPlayer(player, new RequestEmotePackPayload());
    }
}