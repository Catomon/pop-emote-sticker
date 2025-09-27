package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmotePackToClientPacket {
    private final UUID playerUUID;
    private final Map<Integer, byte[]> emotes;

    public EmotePackToClientPacket(UUID playerUUID, Map<Integer, byte[]> emotes) {
        this.playerUUID = playerUUID;
        this.emotes = emotes;
    }

    public EmotePackToClientPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        int mapSize = buf.readInt();
        this.emotes = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            int key = buf.readInt();
            byte[] value = buf.readByteArray();
            this.emotes.put(key, value);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerUUID);
        buf.writeInt(this.emotes.size());
        for (Map.Entry<Integer, byte[]> entry : this.emotes.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeByteArray(entry.getValue());
        }
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
}