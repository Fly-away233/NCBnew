package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class PatternSteelBlock extends Block {
	public PatternSteelBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.COPPER).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}