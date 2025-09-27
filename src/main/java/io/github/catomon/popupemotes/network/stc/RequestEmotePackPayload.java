package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.PopUpEmotes;
import io.github.catomon.popupemotes.network.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestEmotePackPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestEmotePackPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "request_emote_pack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestEmotePackPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestEmotePackPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(RequestEmotePackPayload payload, IPayloadContext context) {
        ClientHandler.handleOnNetwork(payload, context);
    }
}