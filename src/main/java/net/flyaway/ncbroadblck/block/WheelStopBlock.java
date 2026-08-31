package net.flyaway.ncbroadblck.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class WheelStopBlock extends Block implements SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty LOW = BooleanProperty.create("low");

	// 普通高度碰撞箱
	private static final VoxelShape SHAPE_NS = Block.box(0, 0, 5, 16, 4, 11);
	private static final VoxelShape SHAPE_EW = Block.box(5, 0, 0, 11, 4, 16);
	// 低位碰撞箱（放在下半砖上时下沉）
	private static final VoxelShape SHAPE_LOW_NS = Block.box(0, -8, 5, 16, -4, 11);
	private static final VoxelShape SHAPE_LOW_EW = Block.box(5, -8, 0, 11, -4, 16);

	public WheelStopBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.CANDLE).strength(1.5f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(WATERLOGGED, false)
			.setValue(LOW, false));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return state.getFluidState().isEmpty();
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return propagatesSkylightDown(state, worldIn, pos) ? 0 : 1;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		boolean isLow = state.getValue(LOW);
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			return isLow ? SHAPE_LOW_NS : SHAPE_NS;
		} else {
			return isLow ? SHAPE_LOW_EW : SHAPE_EW;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, WATERLOGGED, LOW);
	}

	/** 检测下方方块是否为下半砖 */
	private boolean isBottomSlab(BlockGetter world, BlockPos pos) {
		BlockState stateBelow = world.getBlockState(pos.below());
		if (stateBelow.hasProperty(BlockStateProperties.SLAB_TYPE)) {
			return stateBelow.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null)
			return null;
		boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
		boolean isLow = isBottomSlab(context.getLevel(), context.getClickedPos());
		return state.setValue(FACING, context.getHorizontalDirection().getOpposite())
					.setValue(WATERLOGGED, flag)
					.setValue(LOW, isLow);
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		// 当下方方块发生变化时，重新检测是否为下半砖
		if (facing == Direction.DOWN) {
			return state.setValue(LOW, isBottomSlab(world, currentPos));
		}
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}
}