package net.flyaway.ncbroadblck.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import com.google.common.collect.ImmutableMap;

public class BarrierShapeFBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LOW = BooleanProperty.create("low");

	private final ImmutableMap<BlockState, VoxelShape> shapes = this.makeShapes();

	public BarrierShapeFBlock() {
		super(BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOL)
				.strength(2f, 10f)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(LOW, false));
	}

	/** 检测下方是否为下半砖 */
	private static boolean isBottomSlab(LevelAccessor level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState belowState = level.getBlockState(below);
		return belowState.getBlock() instanceof SlabBlock
				&& belowState.hasProperty(SlabBlock.TYPE)
				&& belowState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
	}

	private ImmutableMap<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			boolean low = state.getValue(LOW);
			// low 时整体 y 轴偏移 -8
			int yOff = low ? -8 : 0;

			return switch (state.getValue(FACING)) {
				case NORTH -> Shapes.or(
						box(0, yOff, 0, 16, 11 + yOff, 16),
						box(0, 11 + yOff, 3, 16, 24 + yOff, 13));
				case EAST -> Shapes.or(
						box(0, yOff, 0, 16, 11 + yOff, 16),
						box(3, 11 + yOff, 0, 13, 24 + yOff, 16));
				case WEST -> Shapes.or(
						box(0, yOff, 0, 16, 11 + yOff, 16),
						box(3, 11 + yOff, 0, 13, 24 + yOff, 16));
				default -> Shapes.or(
						box(0, yOff, 0, 16, 11 + yOff, 16),
						box(0, 11 + yOff, 3, 16, 24 + yOff, 13));
			};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shapes.get(state);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, LOW);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null)
			return null;
		return state
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(LOW, isBottomSlab(context.getLevel(), context.getClickedPos()));
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		// LOW 仅在放置时确定一次，后续方块更新不再改变
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}
}