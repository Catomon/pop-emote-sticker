package io.github.catomon.popupemotes.network;

import io.github.catomon.popupemotes.Config;
import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import io.github.catomon.popupemotes.client.EmoteLayerRenderer;
import io.github.catomon.popupemotes.client.EmoteRenderer;
import io.github.catomon.popupemotes.client.ModSounds;
import io.github.catomon.popupemotes.network.cts.EmotePackUploadPayload;
import io.github.catomon.popupemotes.network.stc.AllPlayersEmotePacksPayload;
import io.github.catomon.popupemotes.network.stc.EmotePackToClientPayload;
import io.github.catomon.popupemotes.network.stc.EmoteToClientPayload;
import io.github.catomon.popupemotes.network.stc.RequestEmotePackPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.UUID;

public class ClientHandler {
    public static void handleOnNetwork(RequestEmotePackPayload payload, IPayloadContext context) {
        // On client: when receiving this request, send emote upload packet to server
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            UUID playerUUID = player.getUUID();
            var emotes = ClientEmotePacksManager.getLocalEmotePack();

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

    // Handler method called on the appropriate thread (usually network thread)
    public static void handleOnNetwork(EmoteToClientPayload payload, IPayloadContext context) {
        // Enqueue work on main thread
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            var player = mc.level.getPlayerByUUID(payload.senderUUID());
            if (player != null) {
                if (Config.useCompatRender) {
                    EmoteLayerRenderer.showEmoteOnPlayer(player.getUUID(), payload.emoteId());
                } else {
                    EmoteRenderer.showEmoteOnPlayer(player.getUUID(), payload.emoteId());
                }
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
            context.disconnect(Component.translatable("pop_up_emotes.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void handleOnNetwork(EmotePackToClientPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null)
                if (Minecraft.getInstance().player.getUUID().equals(payload.playerUUID()))
                    return;

            ClientEmotePacksManager.cachePlayerEmotePack(payload.playerUUID(), payload.emotes());
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public static void handleOnNetwork(AllPlayersEmotePacksPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID localUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            payload.playersEmotePacks().forEach((uuid, emotePack) -> {
                if (!uuid.equals(localUUID)) {
                    ClientEmotePacksManager.cachePlayerEmotePack(uuid, emotePack);
                }
            });
        }).exceptionally(e -> {
            // Handle exceptions gracefully
            e.printStackTrace();
            return null;
        });
    }
}
