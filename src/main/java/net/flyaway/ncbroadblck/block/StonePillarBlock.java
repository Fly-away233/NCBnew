package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class StonePillarBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty LOWERED = BooleanProperty.create("lowered");
	
	private static final VoxelShape SHAPE = box(2, 0, 2, 14, 13, 14);
	private static final VoxelShape LOWERED_SHAPE = box(2, -8, 2, 14, 5, 14);

	public StonePillarBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(2f, 10f).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(LOWERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(LOWERED) ? LOWERED_SHAPE : SHAPE;
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED, LOWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
		BlockState state = super.getStateForPlacement(context).setValue(WATERLOGGED, flag);
		
		BlockPos pos = context.getClickedPos();
		boolean isSlab = isSlabBlock(context.getLevel().getBlockState(pos.below()));
		return state.setValue(LOWERED, isSlab);
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
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!level.isClientSide()) {
			updateConnections(level, pos);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, 
	                            BlockPos neighborPos, boolean isMoving) {
		if (!level.isClientSide()) {
			if (neighborPos.equals(pos.below())) {
				updateConnections(level, pos);
			}
		}
		super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
	}

	private void updateConnections(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.is(this)) return;

		boolean isSlab = isSlabBlock(level.getBlockState(pos.below()));
		if (state.getValue(LOWERED) != isSlab) {
			level.setBlock(pos, state.setValue(LOWERED, isSlab), Block.UPDATE_ALL);
		}
	}

	private boolean isSlabBlock(BlockState state) {
		return state.getBlock() instanceof SlabBlock 
			&& state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
	}
}