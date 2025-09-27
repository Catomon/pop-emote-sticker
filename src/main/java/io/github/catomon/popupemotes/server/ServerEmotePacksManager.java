package io.github.catomon.popupemotes.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerEmotePacksManager {

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

    public static Map<UUID, Map<Integer,byte[]>> getAllPlayerEmotePacks() {
        return playerEmotePacks;
    }
}