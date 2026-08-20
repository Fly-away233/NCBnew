package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;

public class AsphaltYieldLineBlock extends Asphalt3ColorLineBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public AsphaltYieldLineBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).sound(SoundType.BASALT).strength(1.5f, 10f).requiresCorrectToolForDrops());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}
}