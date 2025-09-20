package io.github.catomon.popemotesticker.network;

import io.github.catomon.popemotesticker.PopEmoteSticker;
import io.github.catomon.popemotesticker.client.EmoteRenderer;
import io.github.catomon.popemotesticker.client.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

// Packet sent from server to clients to show emote on a player
public record EmoteToClientPayload(int emoteId, UUID senderUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EmoteToClientPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "emote_to_client"));

    // Define how to encode/decode the payload fields
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

    // Handler method called on the appropriate thread (usually network thread)
    public static void handleOnNetwork(EmoteToClientPayload payload, IPayloadContext context) {
        // Enqueue work on main thread
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            var player = mc.level.getPlayerByUUID(payload.senderUUID());
            if (player != null) {
                EmoteRenderer.showEmoteOnPlayer(player.getUUID(), payload.emoteId());
                mc.level.playLocalSound(
                        player.getX(),
                        player.getY() + player.getEyeHeight(),
                        player.getZ(),
                        ModSounds.EMOTE_SOUND.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F,
                        false
                );
            }
        }).exceptionally(e -> {
            // Optionally handle exceptions and disconnect if needed
            context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
            return null;
        });
    }
}