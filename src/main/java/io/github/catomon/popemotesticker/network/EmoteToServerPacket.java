package io.github.catomon.popemotesticker.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

// Packet sent from client to server to notify an emote trigger
public class EmoteToServerPacket {
    private final int emoteId;
    private final UUID senderUUID;

    public EmoteToServerPacket(int emoteId, UUID senderUUID) {
        this.emoteId = emoteId;
        this.senderUUID = senderUUID;
    }

    public EmoteToServerPacket(FriendlyByteBuf buf) {
        this.emoteId = buf.readInt();
        this.senderUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.emoteId);
        buf.writeUUID(this.senderUUID);
    }

    public static void handle(EmoteToServerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isServer()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            // Broadcast emote packet from server to all tracking clients, including sender
            EmoteToClientPacket broadcastPacket = new EmoteToClientPacket(msg.emoteId, msg.senderUUID);
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                    broadcastPacket
            );
        });

        context.setPacketHandled(true);
    }
}
