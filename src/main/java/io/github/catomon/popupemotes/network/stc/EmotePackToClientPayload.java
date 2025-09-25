package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.PopUpEmotes;
import io.github.catomon.popupemotes.client.EmoteClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record EmotePackToClientPayload(UUID playerUUID, Map<Integer, byte[]> emotes) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EmotePackToClientPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "emote_pack_to_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EmotePackToClientPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, EmotePackToClientPayload::playerUUID,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.BYTE_ARRAY), EmotePackToClientPayload::emotes,
                    EmotePackToClientPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(EmotePackToClientPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player.getUUID().equals(payload.playerUUID))
                return;

            // Client-side: cache or update emote pack for the player UUID
            EmoteClientManager.cachePlayerEmotePack(payload.playerUUID(), payload.emotes());
        }).exceptionally(e -> {
            // Handle errors gracefully
            return null;
        });
    }
}