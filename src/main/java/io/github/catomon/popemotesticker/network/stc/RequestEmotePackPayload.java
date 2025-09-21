package io.github.catomon.popemotesticker.network.stc;

import io.github.catomon.popemotesticker.PopEmoteSticker;
import io.github.catomon.popemotesticker.client.EmoteClientManager;
import io.github.catomon.popemotesticker.network.cts.EmotePackUploadPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestEmotePackPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestEmotePackPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "request_emote_pack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestEmotePackPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestEmotePackPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(RequestEmotePackPayload payload, IPayloadContext context) {
        // On client: when receiving this request, send emote upload packet to server
        context.enqueueWork(() -> {
            System.out.println("RequestEmotePackPayload");
            var player = context.player();
            if (player == null) return;

            var playerUUID = player.getUUID();
            var emotes = EmoteClientManager.getLocalEmotePack();
            EmotePackUploadPayload uploadPayload = new EmotePackUploadPayload(playerUUID, emotes);
            PacketDistributor.sendToServer(uploadPayload);
        }).exceptionally(e -> {
            // Log or handle failure gracefully
            e.printStackTrace();
            return null;
        });
    }
}