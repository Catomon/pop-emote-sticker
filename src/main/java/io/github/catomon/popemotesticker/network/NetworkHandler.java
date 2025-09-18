package io.github.catomon.popemotesticker.network;

import io.github.catomon.popemotesticker.PopEmoteStickerMod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PopEmoteStickerMod.MODID, "main"),
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

        // Serverbound packet: client -> server
        INSTANCE.registerMessage(
                nextPacketId(),
                EmoteToServerPacket.class,
                EmoteToServerPacket::encode,
                EmoteToServerPacket::new,
                EmoteToServerPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
