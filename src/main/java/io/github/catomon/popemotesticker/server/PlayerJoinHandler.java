package io.github.catomon.popemotesticker.server;

import io.github.catomon.popemotesticker.PopEmoteSticker;
import io.github.catomon.popemotesticker.network.NetworkHandler;
import io.github.catomon.popemotesticker.network.stc.AllPlayersEmotePacksPacket;
import io.github.catomon.popemotesticker.network.stc.RequestEmotePackPacket;
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
                new AllPlayersEmotePacksPacket(ServerEmoteManager.getAllPlayerEmotePacks())
        );

        NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new RequestEmotePackPacket()
        );
    }
}