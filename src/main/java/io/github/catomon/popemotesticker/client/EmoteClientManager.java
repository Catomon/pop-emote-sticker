package io.github.catomon.popemotesticker.client;

import io.github.catomon.popemotesticker.PopEmoteSticker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager to cache emote packs for players.
 */
public class EmoteClientManager {
    public static final ResourceLocation[] EMOTE_TEXTURES = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote1.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote2.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote3.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote4.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote5.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote6.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote7.png"),
            ResourceLocation.fromNamespaceAndPath(PopEmoteSticker.MODID, "textures/emotes/emote8.png")
    };

    private static final Map<UUID, Map<Integer, byte[]>> cachedEmotePacks = new ConcurrentHashMap<>();
    private static Map<Integer, byte[]> cachedLocalEmotePack = null;
    private static Map<Integer, byte[]> cachedDefaultEmoteBytes = null;

    /**
     * Returns the local emote pack for the client player.
     * Loads from local disk folder if present and not empty,
     * otherwise falls back to default bundled asset textures.
     */
    public static Map<Integer, byte[]> getLocalEmotePack() {
        if (cachedLocalEmotePack != null && !cachedLocalEmotePack.isEmpty()) {
            return cachedLocalEmotePack;
        }
        // Try load from disk
        Map<Integer, byte[]> fromDisk = loadLocalEmotePack();
        if (!fromDisk.isEmpty()) {
            cachedLocalEmotePack = fromDisk;
            return fromDisk;
        }
        // Fallback: load default emotes from mod assets as byte arrays
        cachedLocalEmotePack = loadDefaultEmotesAsBytes();
        return cachedLocalEmotePack;
    }

    private static Map<Integer, byte[]> loadLocalEmotePack() {
        Map<Integer, byte[]> emotesMap = new HashMap<>();
        try {
            Path baseFolder = getEmotePackFolder();
            for (int i = 1; i <= 8; i++) {
                Path emotePath = baseFolder.resolve("emote" + i + ".png");
                if (Files.exists(emotePath)) {
                    byte[] imageBytes = Files.readAllBytes(emotePath);
                    emotesMap.put(i - 1, imageBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emotesMap;
    }

    public static Map<Integer, byte[]> loadDefaultEmotesAsBytes() {
        if (cachedDefaultEmoteBytes != null) {
            return cachedDefaultEmoteBytes;
        }
        Map<Integer, byte[]> map = new HashMap<>();
        for (int i = 0; i < EMOTE_TEXTURES.length; i++) {
            byte[] bytes = readResourceTextureToBytes(EMOTE_TEXTURES[i]);
            if (bytes != null) {
                map.put(i, bytes);
            }
        }
        cachedDefaultEmoteBytes = map;
        return map;
    }

    /**
     * Reads image bytes from mod resource location.
     * Returns null on failure.
     */
    private static byte[] readResourceTextureToBytes(ResourceLocation resourceLocation) {
        try (var inputStream = Minecraft.getInstance().getResourceManager().open(resourceLocation)) {
            if (inputStream == null) return null;
            return inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cachePlayerEmotePack(UUID playerUUID, Map<Integer, byte[]> emotes) {
        cachedEmotePacks.put(playerUUID, emotes);
        var player = Minecraft.getInstance().player;
        if (player != null && player.getUUID().equals(playerUUID)) {
            cachedLocalEmotePack = emotes;
        }
    }

    public static Map<Integer, byte[]> getPlayerEmotePack(UUID playerUUID) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.getUUID().equals(playerUUID)) {
            // For local player, return the local emote pack with default fallback
            return getLocalEmotePack();
        }
        // For others, return cached pack or empty
        return cachedEmotePacks.getOrDefault(playerUUID, Map.of());
    }

    public static void removePlayerEmotePack(UUID playerUUID) {
        cachedEmotePacks.remove(playerUUID);
        var player = Minecraft.getInstance().player;
        if (player != null && player.getUUID().equals(playerUUID)) {
            cachedLocalEmotePack = null;
        }
    }

    public static Path getEmotePackFolder() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("pop_emote_pack");
    }
}