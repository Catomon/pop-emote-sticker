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

public class AllPlayersEmotePacksPacket {
    public final Map<UUID, Map<Integer, byte[]>> playersEmotePacks;

    public AllPlayersEmotePacksPacket(Map<UUID, Map<Integer, byte[]>> playersEmotePacks) {
        this.playersEmotePacks = playersEmotePacks;
    }

    public AllPlayersEmotePacksPacket(FriendlyByteBuf buf) {
        int outerMapSize = buf.readInt();
        this.playersEmotePacks = new HashMap<>();

        for (int i = 0; i < outerMapSize; i++) {
            UUID playerUUID = buf.readUUID();
            int innerMapSize = buf.readInt();

            Map<Integer, byte[]> emotePack = new HashMap<>();
            for (int j = 0; j < innerMapSize; j++) {
                int emoteKey = buf.readInt();
                byte[] emoteData = buf.readByteArray();
                emotePack.put(emoteKey, emoteData);
            }
            this.playersEmotePacks.put(playerUUID, emotePack);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.playersEmotePacks.size());
        for (Map.Entry<UUID, Map<Integer, byte[]>> outerEntry : this.playersEmotePacks.entrySet()) {
            buf.writeUUID(outerEntry.getKey());
            
            Map<Integer, byte[]> emotePack = outerEntry.getValue();
            buf.writeInt(emotePack.size());
            
            for (Map.Entry<Integer, byte[]> innerEntry : emotePack.entrySet()) {
                buf.writeInt(innerEntry.getKey());
                buf.writeByteArray(innerEntry.getValue());
            }
        }
    }

    public static void handle(AllPlayersEmotePacksPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ClientHandler.handle(packet, ctx);
    }
}