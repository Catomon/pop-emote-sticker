package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.network.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmotePackToClientPacket {
    public final UUID playerUUID;
    public final Map<Integer, byte[]> emotes;

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
        ClientHandler.handle(packet, ctx);
    }
}