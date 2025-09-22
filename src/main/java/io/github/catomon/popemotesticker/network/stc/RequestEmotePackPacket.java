package io.github.catomon.popemotesticker.network.stc;

import io.github.catomon.popemotesticker.client.EmoteClientManager;
import io.github.catomon.popemotesticker.network.NetworkHandler;
import io.github.catomon.popemotesticker.network.cts.EmotePackUploadPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestEmotePackPacket {

    public RequestEmotePackPacket() {
        // Empty constructor since no data is sent
    }

    public RequestEmotePackPacket(FriendlyByteBuf buf) {
        // Empty decoder since no data is present
    }

    public void encode(FriendlyByteBuf buf) {
        // Empty encoder since no data is sent
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
            Map<Integer, byte[]> emotes = EmoteClientManager.getLocalEmotePack(); // Or other server method to retrieve emotes

            EmotePackUploadPacket uploadPacket = new EmotePackUploadPacket(playerUUID, emotes);
            NetworkHandler.INSTANCE.sendToServer(uploadPacket);
        });

        context.setPacketHandled(true);
    }
}