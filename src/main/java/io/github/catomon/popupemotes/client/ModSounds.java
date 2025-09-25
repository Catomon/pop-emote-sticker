package io.github.catomon.popupemotes.client;

import io.github.catomon.popupemotes.PopUpEmotes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PopUpEmotes.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_SOUND = SOUNDS.register("emote_sound",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(PopUpEmotes.MODID, "emote_sound"))
    );

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

}