package io.github.catomon.popemotesticker.server;

import io.github.catomon.popemotesticker.network.stc.EmotePackToClientPayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerEmoteManager {

    // Thread-safe map of player UUID -> emote pack map (emoteId -> image bytes)
    private static final Map<UUID, Map<Integer, byte[]>> playerEmotePacks = new ConcurrentHashMap<>();

    /**
     * Store or update the emote pack bytes for a player.
     */
    public static void setPlayerEmotePack(UUID playerUUID, Map<Integer, byte[]> emotes) {
        playerEmotePacks.put(playerUUID, emotes);
    }

    /**
     * Get the emote pack bytes for a player.
     */
    public static Map<Integer, byte[]> getPlayerEmotePack(UUID playerUUID) {
        return playerEmotePacks.getOrDefault(playerUUID, Map.of());
    }

    /**
     * Remove player's emote pack on track when player logs out.
     */
    public static void removePlayerEmotePack(UUID playerUUID) {
        playerEmotePacks.remove(playerUUID);
    }

    /**
     * Optional: broadcast emote pack updates to other players.
     * Implementation depends on your packet sending utilities.
     */
    public static void broadcastPlayerEmotePackUpdate(UUID playerUUID) {
        Map<Integer, byte[]> emotes = playerEmotePacks.get(playerUUID);
        if (emotes == null) return;

        EmotePackToClientPayload payload = new EmotePackToClientPayload(playerUUID, emotes);
        // TODO Send packet to all clients except the player (implement packet sending accordingly)
        // Example: NetworkChannel.send(PacketDistributor.ALL.noArg(), payload);
    }
}