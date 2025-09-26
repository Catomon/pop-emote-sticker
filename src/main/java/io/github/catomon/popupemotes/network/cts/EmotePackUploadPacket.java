package io.github.catomon.popupemotes.network.cts;

import io.github.catomon.popupemotes.network.NetworkHandler;
import io.github.catomon.popupemotes.network.stc.EmotePackToClientPacket;
import io.github.catomon.popupemotes.server.ServerEmoteManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmotePackUploadPacket {
    private final UUID senderUUID;
    private final Map<Integer, byte[]> emotes;

    public EmotePackUploadPacket(UUID senderUUID, Map<Integer, byte[]> emotes) {
        this.senderUUID = senderUUID;
        this.emotes = emotes;
    }

    public EmotePackUploadPacket(FriendlyByteBuf buf) {
        this.senderUUID = buf.readUUID();
        int mapSize = buf.readInt();
        this.emotes = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            int key = buf.readInt();
            byte[] value = buf.readByteArray();
            this.emotes.put(key, value);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.senderUUID);
        buf.writeInt(this.emotes.size());
        for (Map.Entry<Integer, byte[]> entry : this.emotes.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeByteArray(entry.getValue());
        }
    }

    public static void handle(EmotePackUploadPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (!context.getDirection().getReceptionSide().isServer()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            
            ServerEmoteManager.setPlayerEmotePack(sender.getUUID(), packet.emotes);

            NetworkHandler.INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                new EmotePackToClientPacket(sender.getUUID(), packet.emotes)
            );
        });
        context.setPacketHandled(true);
    }
}