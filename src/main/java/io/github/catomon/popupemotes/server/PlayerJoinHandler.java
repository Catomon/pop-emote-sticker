package io.github.catomon.popupemotes.server;

import io.github.catomon.popupemotes.network.NetworkHandler;
import io.github.catomon.popupemotes.network.stc.AllPlayersEmotePacksPacket;
import io.github.catomon.popupemotes.network.stc.RequestEmotePackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class PlayerJoinHandler {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new AllPlayersEmotePacksPacket(ServerEmotePacksManager.getAllPlayerEmotePacks())
        );

        NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new RequestEmotePackPacket()
        );
    }

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerEmotePacksManager.removePlayerEmotePack(player.getUUID());
    }
}