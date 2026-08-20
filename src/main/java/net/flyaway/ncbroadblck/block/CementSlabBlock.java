package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SlabBlock;

public class CementSlabBlock extends SlabBlock {
	public CementSlabBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}