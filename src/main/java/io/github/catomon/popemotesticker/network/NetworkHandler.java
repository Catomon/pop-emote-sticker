package io.github.catomon.popemotesticker.network;

import io.github.catomon.popemotesticker.network.cts.EmotePackUploadPayload;
import io.github.catomon.popemotesticker.network.cts.EmoteToServerPayload;
import io.github.catomon.popemotesticker.network.stc.AllPlayersEmotePacksPayload;
import io.github.catomon.popemotesticker.network.stc.EmotePackToClientPayload;
import io.github.catomon.popemotesticker.network.stc.EmoteToClientPayload;
import io.github.catomon.popemotesticker.network.stc.RequestEmotePackPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION)
                .executesOn(HandlerThread.NETWORK); // handle packets on network thread by default

        // Server to Client (Clientbound)
        registrar.playToClient(
                EmoteToClientPayload.TYPE,
                EmoteToClientPayload.STREAM_CODEC,
                EmoteToClientPayload::handleOnNetwork
        );

        registrar.playToClient(
                AllPlayersEmotePacksPayload.TYPE,
                AllPlayersEmotePacksPayload.STREAM_CODEC,
                AllPlayersEmotePacksPayload::handleOnNetwork
        );

        registrar.playToClient(
                EmotePackToClientPayload.TYPE,
                EmotePackToClientPayload.STREAM_CODEC,
                EmotePackToClientPayload::handleOnNetwork
        );

        registrar.playToClient(
                RequestEmotePackPayload.TYPE,
                RequestEmotePackPayload.STREAM_CODEC,
                RequestEmotePackPayload::handleOnNetwork
        );

        // Client to Server (Serverbound)
        registrar.playToServer(
                EmoteToServerPayload.TYPE,
                EmoteToServerPayload.STREAM_CODEC,
                EmoteToServerPayload::handleOnNetwork
        );

        registrar.playToServer(
                EmotePackUploadPayload.TYPE,
                EmotePackUploadPayload.STREAM_CODEC,
                EmotePackUploadPayload::handleOnNetwork
        );
    }
}