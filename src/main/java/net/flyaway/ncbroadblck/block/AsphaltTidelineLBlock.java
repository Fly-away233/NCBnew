package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class AsphaltTidelineLBlock extends AsphaltTidelineBlock {
	public AsphaltTidelineLBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).sound(SoundType.BASALT).strength(1.5f, 10f).requiresCorrectToolForDrops());
	}
}