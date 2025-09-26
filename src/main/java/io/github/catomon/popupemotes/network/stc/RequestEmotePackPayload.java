package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.PopUpEmotes;
import io.github.catomon.popupemotes.client.EmoteClientManager;
import io.github.catomon.popupemotes.network.cts.EmotePackUploadPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        // On client: when receiving this request, send emote upload packet to server
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            UUID playerUUID = player.getUUID();
            var emotes = EmoteClientManager.getLocalEmotePack();

            if (emotes == null)
                emotes = new HashMap<>();

            EmotePackUploadPayload uploadPayload = new EmotePackUploadPayload(playerUUID, emotes);
            PacketDistributor.sendToServer(uploadPayload);
        }).exceptionally(e -> {
            // Log or handle failure gracefully
            e.printStackTrace();
            return null;
        });
    }
}