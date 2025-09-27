package io.github.catomon.popupemotes;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = PopUpEmotes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
//    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static boolean useCompatRender = true;

    public static final String CUSTOM_PACK_FOLDER_NAME = "pop_emote_pack";

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

    }
}
