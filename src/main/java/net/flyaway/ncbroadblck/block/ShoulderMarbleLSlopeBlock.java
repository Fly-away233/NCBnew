package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

public class ShoulderMarbleLSlopeBlock extends AsphaltRoadSlopeBlock {
	public ShoulderMarbleLSlopeBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}