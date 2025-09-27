package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import io.github.catomon.popupemotes.network.NetworkHandler;
import io.github.catomon.popupemotes.network.cts.EmotePackUploadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestEmotePackPacket {

    public RequestEmotePackPacket() {
    }

    public RequestEmotePackPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

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
}