package io.github.catomon.popupemotes;

import com.mojang.logging.LogUtils;
import io.github.catomon.popupemotes.client.ClientEmotePacksManager;
import io.github.catomon.popupemotes.client.gui.PopUpEmotesConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(value = PopUpEmotes.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PopUpEmotes.MODID, value = Dist.CLIENT)
public class PopUpEmotesClient {
    public static final Logger LOGGER = LogUtils.getLogger();

    public PopUpEmotesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, screen) -> new PopUpEmotesConfigScreen()
        );
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

        try {
            ClientEmotePacksManager.getEmotePackFolder().toFile().mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
