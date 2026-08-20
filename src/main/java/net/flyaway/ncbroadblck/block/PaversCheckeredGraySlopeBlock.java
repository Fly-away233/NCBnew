package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class PaversCheckeredGraySlopeBlock extends AsphaltRoadSlopeBlock {
	public PaversCheckeredGraySlopeBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE_BRICKS).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}