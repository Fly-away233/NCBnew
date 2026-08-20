package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class AsphaltYellowBackslashBlock extends AsphaltYellowGridlineBlock {
	public AsphaltYellowBackslashBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.BASALT).strength(1.5f, 10f).requiresCorrectToolForDrops());
	}
}