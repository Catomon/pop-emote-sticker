package io.github.catomon.popupemotes.network;

import io.github.catomon.popupemotes.PopUpEmotes;
import io.github.catomon.popupemotes.network.cts.EmotePackUploadPacket;
import io.github.catomon.popupemotes.network.cts.EmoteToServerPacket;
import io.github.catomon.popupemotes.network.stc.AllPlayersEmotePacksPacket;
import io.github.catomon.popupemotes.network.stc.EmotePackToClientPacket;
import io.github.catomon.popupemotes.network.stc.EmoteToClientPacket;
import io.github.catomon.popupemotes.network.stc.RequestEmotePackPacket;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextPacketId() {
        return packetId++;
    }

    public static void register() {
        // Clientbound packet: server -> client
        INSTANCE.registerMessage(
                nextPacketId(),
                EmoteToClientPacket.class,
                EmoteToClientPacket::encode,
                EmoteToClientPacket::new,
                EmoteToClientPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextPacketId(),
                AllPlayersEmotePacksPacket.class,
                AllPlayersEmotePacksPacket::encode,
                AllPlayersEmotePacksPacket::new,
                AllPlayersEmotePacksPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextPacketId(),
                EmotePackToClientPacket.class,
                EmotePackToClientPacket::encode,
                EmotePackToClientPacket::new,
                EmotePackToClientPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                nextPacketId(),
                RequestEmotePackPacket.class,
                RequestEmotePackPacket::encode,
                RequestEmotePackPacket::new,
                RequestEmotePackPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // Serverbound packet: client -> server
        INSTANCE.registerMessage(
                nextPacketId(),
                EmoteToServerPacket.class,
                EmoteToServerPacket::encode,
                EmoteToServerPacket::new,
                EmoteToServerPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(
                nextPacketId(),
                EmotePackUploadPacket.class,
                EmotePackUploadPacket::encode,
                EmotePackUploadPacket::new,
                EmotePackUploadPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
