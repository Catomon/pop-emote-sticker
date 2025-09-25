package io.github.catomon.popupemotes;

import com.mojang.logging.LogUtils;
import io.github.catomon.popupemotes.client.EmoteClientManager;
import io.github.catomon.popupemotes.client.ModSounds;
import io.github.catomon.popupemotes.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// Main entrypoint for the "Pop Emote Sticker" mod.
// Make sure your modId is 'pop_up_emotes' in both mods.toml and this class.
@Mod(PopEmoteSticker.MODID)
public class PopEmoteSticker {

    // --- MOD CONSTANTS ---
    // Unique mod ID. Used for registry namespaces and must match mods.toml.
    public static final String MODID = "pop_up_emotes";

    // Logger for debug/information output.
    private static final Logger LOGGER = LogUtils.getLogger();

    /* --------- REGISTRIES: Uncomment/add only what you actually use --------- */

    // Remove block/item/creative tab registration if your mod won't use these yet.
    // public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /* --------- EXAMPLE REGISTRY OBJECTS: Commented out (not needed for emote mod) --------- */

    // // Example of registering a block.
    // public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
    //         () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    // // Example of registering a block item.
    // public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
    //         () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // // Example of registering an item (could be used for an emote selection tool if desired).
    // public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
    //         () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));

    // // Example creative tab for items/blocks (not needed for an emote-only mod by default).
    // public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab",
    //         () -> CreativeModeTab.builder()
    //                 .withTabsBefore(CreativeModeTabs.COMBAT)
    //                 .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
    //                 .displayItems((parameters, output) -> output.accept(EXAMPLE_ITEM.get()))
    //                 .build());

    // Constructor: this runs once at mod load and is used for registering setup/event code.
    public PopEmoteSticker(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register mod-wide setup event (for network, config, etc.):
        modEventBus.addListener(this::commonSetup);

        // If you add blocks/items/creative tabs in the future, register the appropriate DeferredRegister here:
        // BLOCKS.register(modEventBus);
        // ITEMS.register(modEventBus);
        // CREATIVE_MODE_TABS.register(modEventBus);

        // Register for global Forge events (required if handling gameplay events).
        MinecraftForge.EVENT_BUS.register(this);

        // If you later want to modify creative tabs, use something like this:
        // modEventBus.addListener(this::addCreative);

        // If using Forge config files, register a config here:
        // context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModSounds.register(context.getModEventBus());
    }

    // Called during common setup. Use this for network registration, custom event hooks, etc.
    private void commonSetup(final FMLCommonSetupEvent event) {
//        LOGGER.info("[PopEmoteSticker] Common setup starting");

        // Place networking registration, emote config loading, etc. here.

        // Sample code below is commented - remove after making your own setup logic.
        // if (Config.logDirtBlock)
        //     LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        // LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        // Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));

        NetworkHandler.register();
    }

    // Example: This would add items to a creative tab. Disable unless needed for emote-related items.
    // private void addCreative(BuildCreativeModeTabContentsEvent event) {
    //     if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
    //         event.accept(EXAMPLE_BLOCK_ITEM);
    // }

    // Example: Listens for server start event, good for advanced mod/server logic.
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
//        LOGGER.info("[PopEmoteSticker] Server is starting!");
        // Setup emote system for server here, if needed.
    }

    // --- CLIENT EVENTS: Setup for registering keybinds, client GUIs, etc. ---
    // Register all client-specific setup or event listeners in this static class.
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
//            LOGGER.info("[PopEmoteSticker] Client setup starting");
            // Register your keybinds for showing the emote pie menu here.
            // Register GUIs, render handlers for emote stickers, etc.

            try {
                EmoteClientManager.getEmotePackFolder().toFile().mkdirs();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}