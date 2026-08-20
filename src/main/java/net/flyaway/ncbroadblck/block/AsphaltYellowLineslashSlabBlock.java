package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;

public class AsphaltYellowLineslashSlabBlock extends SlabBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty RIGHT = BooleanProperty.create("right");

	public AsphaltYellowLineslashSlabBlock() {
		this(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.sound(SoundType.BASALT)
			.strength(1.5f, 10f)
			.requiresCorrectToolForDrops());
	}

	protected AsphaltYellowLineslashSlabBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
			.setValue(FACING, Direction.NORTH)
			.setValue(RIGHT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, RIGHT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) {
			state = this.defaultBlockState();
		}

		state = state.setValue(FACING, context.getHorizontalDirection().getOpposite());

		Direction facing = state.getValue(FACING);
		Direction rightDir = facing.getCounterClockWise();
		boolean hasRight = context.getLevel().getBlockState(context.getClickedPos().relative(rightDir)).getBlock() == this;
		state = state.setValue(RIGHT, hasRight);

		return state;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
}