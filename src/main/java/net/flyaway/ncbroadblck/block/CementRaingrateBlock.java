package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

public class CementRaingrateBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST);

	public static final BooleanProperty LEFT = BooleanProperty.create("left");
	public static final BooleanProperty RIGHT = BooleanProperty.create("right");

	public CementRaingrateBlock() {
		super(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_GRAY)
				.strength(2f, 10f)
				.requiresCorrectToolForDrops());
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(WATERLOGGED, false)
				.setValue(FACING, Direction.NORTH)
				.setValue(LEFT, false)
				.setValue(RIGHT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED, FACING, LEFT, RIGHT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) return null;

		boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
		state = state.setValue(WATERLOGGED, flag);

		Direction dir = context.getHorizontalDirection().getOpposite();
		if (dir == Direction.NORTH || dir == Direction.SOUTH) {
			state = state.setValue(FACING, Direction.NORTH);
		} else {
			state = state.setValue(FACING, Direction.EAST);
		}

		return updateConnections(state, context.getLevel(), context.getClickedPos());
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
								  LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return updateConnections(state, world, currentPos);
	}

	protected BlockState updateConnections(BlockState state, LevelAccessor world, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		Direction leftDir = facing.getCounterClockWise();
		Direction rightDir = facing.getClockWise();

		boolean left = canConnect(world.getBlockState(pos.relative(leftDir)));
		boolean right = canConnect(world.getBlockState(pos.relative(rightDir)));

		return state.setValue(LEFT, left).setValue(RIGHT, right);
	}

	protected boolean canConnect(BlockState state) {
		return state.getBlock() == this;
	}
}