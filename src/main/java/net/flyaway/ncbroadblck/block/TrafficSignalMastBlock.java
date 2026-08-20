package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficSignalMastBlock extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty FRONT = BooleanProperty.create("front");
	public static final BooleanProperty BACK = BooleanProperty.create("back");

	private static final VoxelShape SHAPE_Z = box(5, 5, 0, 11, 11, 16);
	private static final VoxelShape SHAPE_X = box(0, 5, 5, 16, 11, 11);

	public TrafficSignalMastBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(2f, 10f).requiresCorrectToolForDrops()
			.noOcclusion()
			.isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(FRONT, false)
			.setValue(BACK, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FRONT, BACK);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
			return null;
		}

		BlockPos pos = context.getClickedPos();
		BlockGetter world = context.getLevel();

		Direction front = clickedFace;
		Direction back = clickedFace.getOpposite();

		boolean hasFront = world.getBlockState(pos.relative(front)).is(this);
		boolean hasBack = world.getBlockState(pos.relative(back)).is(this);

		return this.defaultBlockState()
			.setValue(FACING, clickedFace)
			.setValue(FRONT, hasFront)
			.setValue(BACK, hasBack);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		Direction front = state.getValue(FACING);
		Direction back = front.getOpposite();

		if (facing == front) {
			return state.setValue(FRONT, facingState.is(this));
		}
		if (facing == back) {
			return state.setValue(BACK, facingState.is(this));
		}
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
}