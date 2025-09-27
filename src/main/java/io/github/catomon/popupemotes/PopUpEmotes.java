package io.github.catomon.popupemotes;

import com.mojang.logging.LogUtils;
import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import io.github.catomon.popupemotes.client.ModSounds;
import io.github.catomon.popupemotes.client.gui.PopUpEmotesConfigScreen;
import io.github.catomon.popupemotes.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PopUpEmotes.MODID)
public class PopUpEmotes {

    public static final String MODID = "pop_up_emotes";

    private static final Logger LOGGER = LogUtils.getLogger();

    public PopUpEmotes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModSounds.register(context.getModEventBus());

        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new PopUpEmotesConfigScreen())
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            try {
                ClientEmotePacksManager.getEmotePackFolder().toFile().mkdirs();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}