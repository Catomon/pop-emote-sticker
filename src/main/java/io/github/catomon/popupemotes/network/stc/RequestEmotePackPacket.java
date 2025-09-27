package io.github.catomon.popupemotes.network.stc;

import io.github.catomon.popupemotes.network.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEmotePackPacket {

    public RequestEmotePackPacket() {
    }

    public RequestEmotePackPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(RequestEmotePackPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ClientHandler.handle(packet, ctx);
    }
}