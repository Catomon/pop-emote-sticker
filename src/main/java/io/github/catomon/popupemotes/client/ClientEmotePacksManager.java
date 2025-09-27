package io.github.catomon.popupemotes.client;

import io.github.catomon.popupemotes.PopUpEmotes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.catomon.popupemotes.Config.CUSTOM_PACK_FOLDER_NAME;

/**
 * Client-side manager to cache emote packs for players.
 */
public class ClientEmotePacksManager {
    public static final ResourceLocation[] EMOTE_TEXTURES = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote1.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote2.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote3.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote4.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote5.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote6.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote7.png"),
            ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "textures/emotes/emote8.png")
    };

    private static final Map<UUID, Map<Integer, byte[]>> cachedEmotePacks = new ConcurrentHashMap<>();
    private static Map<Integer, byte[]> cachedLocalEmotePack = null;
    private static Map<Integer, byte[]> cachedDefaultEmoteBytes = null;
    
    public static @Nullable Map<Integer, byte[]> getLocalEmotePack() {
        if (cachedLocalEmotePack != null && !cachedLocalEmotePack.isEmpty()) {
            return cachedLocalEmotePack;
        }
        Map<Integer, byte[]> fromDisk = loadLocalEmotePack();
        if (!fromDisk.isEmpty()) {
            cachedLocalEmotePack = fromDisk;
            return fromDisk;
        }
        return cachedLocalEmotePack;
    }

    public static void clearCache() {
        cachedEmotePacks.clear();
        cachedLocalEmotePack = null;
        cachedDefaultEmoteBytes = null;
    }

    public static void recreateCache() {
        clearCache();

        getLocalEmotePack();
        loadDefaultEmotesAsBytes();
    }

    private static Map<Integer, byte[]> loadLocalEmotePack() {
        Map<Integer, byte[]> emotesMap = new HashMap<>();
        try {
            Path baseFolder = getEmotePackFolder();

            DirectoryStream.Filter<Path> filter = entry -> {
                String name = entry.getFileName().toString().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg");
            };

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseFolder, filter)) {
                int index = 0;
                for (Path entry : stream) {
                    if (index >= 8) break;
                    try {
                        byte[] imageBytes = Files.readAllBytes(entry);
                        emotesMap.put(index++, imageBytes);
                    } catch (IOException e) {
                        System.err.println("Failed to load emote image " + entry + ": " + e.getMessage());
                    }
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

    /*
    * For local player, return the local emote pack
    * For others, return cached pack or empty
    */
    public static Map<Integer, byte[]> getPlayerEmotePack(UUID playerUUID) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.getUUID().equals(playerUUID)) {
            return getLocalEmotePack();
        }
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
        return Minecraft.getInstance().gameDirectory.toPath().resolve(CUSTOM_PACK_FOLDER_NAME);
    }
}
