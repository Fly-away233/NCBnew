package net.flyaway.ncbroadblck.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import com.google.common.collect.ImmutableMap;

public class SignLaneMotorVehiclesBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LEFT = BooleanProperty.create("left");
	public static final BooleanProperty RIGHT = BooleanProperty.create("right");
	
	private final ImmutableMap<BlockState, VoxelShape> shapes = this.makeShapes();

	public SignLaneMotorVehiclesBlock() {
		super(BlockBehaviour.Properties.of()
			.sound(SoundType.COPPER)
			.strength(2f, 10f)
			.requiresCorrectToolForDrops()
			.noOcclusion()
			.isRedstoneConductor((bs, br, bp) -> false)
			.isViewBlocking((bs, br, bp) -> false));  // <-- 在这里配置，不要写成 @Override 方法
		
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(LEFT, false)
			.setValue(RIGHT, false));
	}

	private ImmutableMap<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			return switch (state.getValue(FACING)) {
				case NORTH -> box(0, 0, 15, 16, 16, 16);
				case EAST  -> box(0, 0, 0, 1, 16, 16);
				case WEST  -> box(15, 0, 0, 16, 16, 16);
				default    -> box(0, 0, 0, 16, 16, 1);
			};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shapes.get(state);
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

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, LEFT, RIGHT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) return null;
		
		state = state.setValue(FACING, context.getHorizontalDirection().getOpposite());
		return calculateConnectionState(state, context.getLevel(), context.getClickedPos());
	}
	
	protected BlockState calculateConnectionState(BlockState state, LevelAccessor world, BlockPos pos) {
		Direction facing  = state.getValue(FACING);
		Direction leftDir = facing.getCounterClockWise();
		Direction rightDir = facing.getClockWise();
		
		boolean hasLeft  = isSameConnectableBlock(world.getBlockState(pos.relative(leftDir)));
		boolean hasRight = isSameConnectableBlock(world.getBlockState(pos.relative(rightDir)));
		
		return state.setValue(LEFT, hasLeft).setValue(RIGHT, hasRight);
	}
	
	protected boolean isSameConnectableBlock(BlockState state) {
		return state.getBlock() instanceof SignLaneMotorVehiclesBlock;
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!oldState.is(state.getBlock())) {
			Direction facing = state.getValue(FACING);
			level.updateNeighborsAt(pos.relative(facing.getCounterClockWise()), this);
			level.updateNeighborsAt(pos.relative(facing.getClockWise()), this);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
		
		Direction facing   = state.getValue(FACING);
		Direction leftDir  = facing.getCounterClockWise();
		Direction rightDir = facing.getClockWise();
		
		if (!fromPos.equals(pos.relative(leftDir)) && !fromPos.equals(pos.relative(rightDir))) {
			return;
		}
		
		BlockState newState = calculateConnectionState(state, level, pos);
		if (newState != state) {
			level.setBlock(pos, newState, 3);
		}
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
}