package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class PaversCheckeredGreenPlateBlock extends AsphaltRoadPlateBlock {
	public PaversCheckeredGreenPlateBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.GLOW_LICHEN).sound(SoundType.DEEPSLATE_BRICKS).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}