package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.network.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// Packet sent from server to clients to show emote on a player
public class EmoteToClientPacket {
    public final int emoteId;
    public final UUID senderUUID;

    public EmoteToClientPacket(int emoteId, UUID senderUUID) {
        this.emoteId = emoteId;
        this.senderUUID = senderUUID;
    }

    public EmoteToClientPacket(FriendlyByteBuf buf) {
        this.emoteId = buf.readInt();
        this.senderUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.emoteId);
        buf.writeUUID(this.senderUUID);
    }

    public static void handle(EmoteToClientPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ClientHandler.handle(packet, ctx);
    }
}
