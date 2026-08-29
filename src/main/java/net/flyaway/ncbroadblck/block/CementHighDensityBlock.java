package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

public class CementHighDensityBlock extends Block {
	public CementHighDensityBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}