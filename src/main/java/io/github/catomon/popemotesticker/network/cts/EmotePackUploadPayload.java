package io.github.catomon.popemotesticker.network.cts;

import io.github.catomon.popemotesticker.PopEmoteSticker;
import io.github.catomon.popemotesticker.network.stc.EmotePackToClientPayload;
import io.github.catomon.popemotesticker.server.ServerEmoteManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record EmotePackUploadPayload(UUID senderUUID, Map<Integer, byte[]> emotes) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EmotePackUploadPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "emote_pack_upload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EmotePackUploadPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, EmotePackUploadPayload::senderUUID,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.BYTE_ARRAY), EmotePackUploadPayload::emotes,
                    EmotePackUploadPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(EmotePackUploadPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            System.out.println("EmotePackUploadPayload");

            ServerPlayer sender = (ServerPlayer) context.player();

            // Store or update emote pack for this player on server
            ServerEmoteManager.setPlayerEmotePack(sender.getUUID(), payload.emotes());

            // Broadcast emote pack metadata or usage info to other players as needed
            // Optionally send small metadata packets and send full data only when requested
            PacketDistributor.sendToAllPlayers(new EmotePackToClientPayload(sender.getUUID(), payload.emotes()));
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
            return null;
        });
    }
}