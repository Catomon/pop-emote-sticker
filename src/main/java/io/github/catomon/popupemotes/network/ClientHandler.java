package io.github.catomon.popupemotes.network;

import io.github.catomon.popupemotes.Config;
import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import io.github.catomon.popupemotes.client.EmoteLayerRenderer;
import io.github.catomon.popupemotes.client.EmoteRenderer;
import io.github.catomon.popupemotes.client.ModSounds;
import io.github.catomon.popupemotes.network.cts.EmotePackUploadPacket;
import io.github.catomon.popupemotes.network.stc.AllPlayersEmotePacksPacket;
import io.github.catomon.popupemotes.network.stc.EmotePackToClientPacket;
import io.github.catomon.popupemotes.network.stc.EmoteToClientPacket;
import io.github.catomon.popupemotes.network.stc.RequestEmotePackPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientHandler {
    public static void handle(RequestEmotePackPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            UUID playerUUID = player.getUUID();
            Map<Integer, byte[]> emotes = ClientEmotePacksManager.getLocalEmotePack();

            if (emotes == null)
                emotes = new HashMap<>();

            EmotePackUploadPacket uploadPacket = new EmotePackUploadPacket(playerUUID, emotes);
            NetworkHandler.INSTANCE.sendToServer(uploadPacket);
        });

        context.setPacketHandled(true);
    }

    public static void handle(EmotePackToClientPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null)
                if (Minecraft.getInstance().player.getUUID().equals(packet.playerUUID))
                    return;

            ClientEmotePacksManager.cachePlayerEmotePack(packet.playerUUID, packet.emotes);
        });

        context.setPacketHandled(true);
    }

    public static void handle(AllPlayersEmotePacksPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            UUID localUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            packet.playersEmotePacks.forEach((uuid, emotePack) -> {
                if (!uuid.equals(localUUID)) {
                    ClientEmotePacksManager.cachePlayerEmotePack(uuid, emotePack);
                }
            });
        });

        context.setPacketHandled(true);
    }

    public static void handle(EmoteToClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Player player = mc.level.getPlayerByUUID(msg.senderUUID);
            if (player != null) {
                if (Config.useCompatRender) {
                    EmoteLayerRenderer.showEmoteOnPlayer(player.getUUID(), msg.emoteId);
                } else {
                    EmoteRenderer.showEmoteOnPlayer(player.getUUID(), msg.emoteId);
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
        });

        context.setPacketHandled(true);
    }
}
