package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SlabBlock;

public class PaversSquareRedSlabBlock extends PaversCheckeredGraySlabBlock {
	public PaversSquareRedSlabBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_MAGENTA).sound(SoundType.DEEPSLATE_BRICKS).strength(2f, 10f).requiresCorrectToolForDrops());
	}
}