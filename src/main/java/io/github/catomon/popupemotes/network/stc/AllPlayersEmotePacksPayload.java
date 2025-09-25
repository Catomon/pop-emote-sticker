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

public record AllPlayersEmotePacksPayload(
        Map<UUID, Map<Integer, byte[]>> playersEmotePacks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AllPlayersEmotePacksPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "all_players_emote_packs"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AllPlayersEmotePacksPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new,
                            UUIDUtil.STREAM_CODEC,
                            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.BYTE_ARRAY)
                    ),
                    AllPlayersEmotePacksPayload::playersEmotePacks,
                    AllPlayersEmotePacksPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnNetwork(AllPlayersEmotePacksPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID localUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            payload.playersEmotePacks().forEach((uuid, emotePack) -> {
                if (!uuid.equals(localUUID)) {
                    EmoteClientManager.cachePlayerEmotePack(uuid, emotePack);
                }
            });
        }).exceptionally(e -> {
            // Handle exceptions gracefully
            e.printStackTrace();
            return null;
        });
    }
}