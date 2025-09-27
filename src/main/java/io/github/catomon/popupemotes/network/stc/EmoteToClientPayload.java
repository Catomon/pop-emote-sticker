package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.PopUpEmotes;
import io.github.catomon.popupemotes.network.ClientHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

// Packet sent from server to clients to show emote on a player
public record EmoteToClientPayload(int emoteId, UUID senderUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EmoteToClientPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "emote_to_client"));

    public static final StreamCodec<FriendlyByteBuf, EmoteToClientPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            EmoteToClientPayload::emoteId,
            UUIDUtil.STREAM_CODEC,
            EmoteToClientPayload::senderUUID,
            EmoteToClientPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(EmoteToClientPayload payload, IPayloadContext context) {
        ClientHandler.handleOnNetwork(payload, context);
    }

    @Override
    public int emoteId() {
        return emoteId;
    }

    @Override
    public UUID senderUUID() {
        return senderUUID;
    }
}