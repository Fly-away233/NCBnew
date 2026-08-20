/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.flyaway.ncbroadblck.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.flyaway.ncbroadblck.NcbRoadblckMod;

public class NcbRoadblckModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, NcbRoadblckMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_WRENCH_USE = REGISTRY.register("electric_wrench_use", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "electric_wrench_use")));
}