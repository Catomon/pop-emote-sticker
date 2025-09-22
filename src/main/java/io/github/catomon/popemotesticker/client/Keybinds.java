package io.github.catomon.popemotesticker.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.catomon.popemotesticker.client.gui.PieMenuScreen;
import io.github.catomon.popemotesticker.network.cts.EmoteToServerPacket;
import io.github.catomon.popemotesticker.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Keybinds {

    // Key mapping for V key
    public static final Lazy<KeyMapping> EMOTE_MENU_KEY = Lazy.of(() -> new KeyMapping(
            "key.popemotesticker.piemenu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.popemotesticker"
    ));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(EMOTE_MENU_KEY.get());
    }
}

@Mod.EventBusSubscriber(value = Dist.CLIENT)
class KeyInputHandler {
    private static boolean menuOpen = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping key = Keybinds.EMOTE_MENU_KEY.get();

        // Only react to the key we're interested in:
        if (event.getKey() == key.getKey().getValue()) {
            if (event.getAction() == GLFW.GLFW_PRESS && !menuOpen) {
                // Key pressed: open menu if not open
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new PieMenuScreen());
                    menuOpen = true;
                }
            } else if (event.getAction() == GLFW.GLFW_RELEASE && menuOpen) {
                // Key released: close menu and send selected emote
                if (mc.screen instanceof PieMenuScreen) {
                    int selectedEmoteId = getSelectedEmoteId();

                    mc.setScreen(null);
                    menuOpen = false;

                    if (selectedEmoteId != -1) {
                        UUID playerUUID = mc.player != null ? mc.player.getUUID() : null;
                        NetworkHandler.INSTANCE.sendToServer(new EmoteToServerPacket(selectedEmoteId, playerUUID));
                    }
                } else {
                    menuOpen = false; // Screen changed unexpectedly
                }
            }
        }
    }

    private static int getSelectedEmoteId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PieMenuScreen) {
            return PieMenuScreen.selectedSlice;
        }
        return -1;
    }
}