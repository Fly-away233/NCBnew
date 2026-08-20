package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class PaversInterlockRedPlateBlock extends AsphaltRoadPlateBlock {
	public PaversInterlockRedPlateBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_MAGENTA).sound(SoundType.DEEPSLATE_BRICKS).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}