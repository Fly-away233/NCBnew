package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

public class ShoulderAsphaltLSlopeBlock extends AsphaltRoadSlopeBlock {
	public ShoulderAsphaltLSlopeBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}