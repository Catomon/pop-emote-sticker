package io.github.catomon.popupemotes.network.cts;

import io.github.catomon.popupemotes.PopUpEmotes;
import io.github.catomon.popupemotes.network.stc.EmoteToClientPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

// Packet sent from client to server to notify an emote trigger
public record EmoteToServerPayload(int emoteId, UUID senderUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EmoteToServerPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "emote_to_server"));

    public static final StreamCodec<FriendlyByteBuf, EmoteToServerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            EmoteToServerPayload::emoteId,
            UUIDUtil.STREAM_CODEC,
            EmoteToServerPayload::senderUUID,
            EmoteToServerPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(EmoteToServerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender == null) return;

            // Broadcast emote payload from server to all tracking clients including sender
            EmoteToClientPayload broadcastPayload = new EmoteToClientPayload(payload.emoteId(), payload.senderUUID());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(sender, broadcastPayload);
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
            return null;
        });
    }
}