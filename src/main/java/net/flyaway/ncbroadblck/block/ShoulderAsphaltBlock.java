package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShoulderAsphaltBlock extends StairBlock {

    private static final VoxelShape STRAIGHT_EAST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 0, 16, 24, 16)
    );
    private static final VoxelShape STRAIGHT_WEST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 5, 24, 16)
    );
    private static final VoxelShape STRAIGHT_SOUTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 11, 16, 24, 16)
    );
    private static final VoxelShape STRAIGHT_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 16, 24, 5)
    );

    private static final VoxelShape INNER_RIGHT_EAST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 0, 16, 24, 16),
            Block.box(0, 16, 11, 11, 24, 16)
    );
    private static final VoxelShape INNER_RIGHT_WEST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 5, 24, 16),
            Block.box(5, 16, 0, 16, 24, 5)
    );
    private static final VoxelShape INNER_RIGHT_SOUTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 11, 16, 24, 16),
            Block.box(0, 16, 0, 5, 24, 11)
    );
    private static final VoxelShape INNER_RIGHT_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 16, 24, 5),
            Block.box(11, 16, 5, 16, 24, 16)
    );

    private static final VoxelShape INNER_LEFT_EAST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 0, 16, 24, 16),
            Block.box(0, 16, 0, 11, 24, 5)
    );
    private static final VoxelShape INNER_LEFT_WEST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 5, 24, 16),
            Block.box(5, 16, 11, 16, 24, 16)
    );
    private static final VoxelShape INNER_LEFT_SOUTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 11, 16, 24, 16),
            Block.box(11, 16, 0, 16, 24, 11)
    );
    private static final VoxelShape INNER_LEFT_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 16, 24, 5),
            Block.box(0, 16, 5, 5, 24, 16)
    );

    private static final VoxelShape OUTER_RIGHT_EAST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 11, 16, 24, 16)
    );
    private static final VoxelShape OUTER_RIGHT_WEST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 5, 24, 5)
    );
    private static final VoxelShape OUTER_RIGHT_SOUTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 11, 5, 24, 16)
    );
    private static final VoxelShape OUTER_RIGHT_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 0, 16, 24, 5)
    );

    private static final VoxelShape OUTER_LEFT_EAST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 0, 16, 24, 5)
    );
    private static final VoxelShape OUTER_LEFT_WEST = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 11, 5, 24, 16)
    );
    private static final VoxelShape OUTER_LEFT_SOUTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(11, 16, 11, 16, 24, 16)
    );
    private static final VoxelShape OUTER_LEFT_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16),
            Block.box(0, 16, 0, 5, 24, 5)
    );

    public ShoulderAsphaltBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                .strength(2f, 10f)
                .requiresCorrectToolForDrops());
    }

    ShoulderAsphaltBlock(BlockBehaviour.Properties properties) {
        super(Blocks.AIR.defaultBlockState(), properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            state = state.setValue(HALF, Half.BOTTOM);
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getCustomShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getCustomShape(state);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getCustomShape(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getCustomShape(state);
    }

    protected VoxelShape getCustomShape(BlockState state) {
        Direction facing = state.getValue(FACING);
        StairsShape shape = state.getValue(SHAPE);

        return switch (shape) {
            case STRAIGHT -> switch (facing) {
                case EAST -> STRAIGHT_EAST;
                case WEST -> STRAIGHT_WEST;
                case SOUTH -> STRAIGHT_SOUTH;
                case NORTH -> STRAIGHT_NORTH;
                default -> STRAIGHT_EAST;
            };
            case INNER_LEFT -> switch (facing) {
                case EAST -> INNER_LEFT_EAST;
                case WEST -> INNER_LEFT_WEST;
                case SOUTH -> INNER_LEFT_SOUTH;
                case NORTH -> INNER_LEFT_NORTH;
                default -> INNER_LEFT_EAST;
            };
            case INNER_RIGHT -> switch (facing) {
                case EAST -> INNER_RIGHT_EAST;
                case WEST -> INNER_RIGHT_WEST;
                case SOUTH -> INNER_RIGHT_SOUTH;
                case NORTH -> INNER_RIGHT_NORTH;
                default -> INNER_RIGHT_EAST;
            };
            case OUTER_LEFT -> switch (facing) {
                case EAST -> OUTER_LEFT_EAST;
                case WEST -> OUTER_LEFT_WEST;
                case SOUTH -> OUTER_LEFT_SOUTH;
                case NORTH -> OUTER_LEFT_NORTH;
                default -> OUTER_LEFT_EAST;
            };
            case OUTER_RIGHT -> switch (facing) {
                case EAST -> OUTER_RIGHT_EAST;
                case WEST -> OUTER_RIGHT_WEST;
                case SOUTH -> OUTER_RIGHT_SOUTH;
                case NORTH -> OUTER_RIGHT_NORTH;
                default -> OUTER_RIGHT_EAST;
            };
            default -> STRAIGHT_EAST;
        };
    }

    @Override
    public float getExplosionResistance() {
        return 10f;
    }
}