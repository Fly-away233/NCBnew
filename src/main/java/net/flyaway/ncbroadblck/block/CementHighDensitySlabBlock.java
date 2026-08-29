package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SlabBlock;

public class CementHighDensitySlabBlock extends SlabBlock {
	public CementHighDensitySlabBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}