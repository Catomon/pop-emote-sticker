package io.github.catomon.popupemotes;

import com.mojang.logging.LogUtils;
import io.github.catomon.popupemotes.client.ModSounds;
import io.github.catomon.popupemotes.client.gui.PopUpEmotesConfigScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(PopUpEmotes.MODID)
public class PopUpEmotes {
    public static final String MODID = "pop_up_emotes";

    public static final Logger LOGGER = LogUtils.getLogger();

    public PopUpEmotes(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ModSounds.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
