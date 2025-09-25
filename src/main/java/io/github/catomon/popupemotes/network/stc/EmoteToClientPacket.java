package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.client.EmoteRenderer;
import io.github.catomon.popupemotes.client.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// Packet sent from server to clients to show emote on a player
public class EmoteToClientPacket {
    private final int emoteId;
    private final UUID senderUUID;

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
                EmoteRenderer.showEmoteOnPlayer(player.getUUID(), msg.emoteId);
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
