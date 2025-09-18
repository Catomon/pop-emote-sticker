package io.github.catomon.popemotesticker.client;

import io.github.catomon.popemotesticker.PopEmoteStickerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PopEmoteStickerMod.MODID);

    public static final RegistryObject<SoundEvent> EMOTE_SOUND = SOUNDS.register("emote_sound", () ->
        SoundEvent.createVariableRangeEvent(new ResourceLocation(PopEmoteStickerMod.MODID, "emote_sound"))
    );

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}