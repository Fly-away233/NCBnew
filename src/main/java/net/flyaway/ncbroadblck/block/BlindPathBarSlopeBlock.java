package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class BlindPathBarSlopeBlock extends AsphaltRoadSlopeBlock {
	public BlindPathBarSlopeBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.AMETHYST).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}