package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpeedBumpBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<SpeedBumpShape> SHAPE = EnumProperty.create("shape", SpeedBumpShape.class);
    public static final BooleanProperty LOW = BooleanProperty.create("low");

    public enum SpeedBumpShape implements StringRepresentable {
        MIDDLE("middle"),
        LEFT("left"),
        RIGHT("right");

        private final String name;

        SpeedBumpShape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    // facing=east/west (y=0, along x-axis) — height 2.5, NOT lowered
    private static final VoxelShape MIDDLE_EW     = box(0, 0, 3, 16, 2.5, 13);
    private static final VoxelShape LEFT_EW       = box(0, 0, 3, 16, 2.5, 13);
    private static final VoxelShape RIGHT_EW      = box(0, 0, 3, 16, 2.5, 13);

    // facing=south/north (y=90, along z-axis) — height 2.5, NOT lowered
    private static final VoxelShape MIDDLE_SN     = box(3, 0, 0, 13, 2.5, 16);
    private static final VoxelShape LEFT_SN       = box(3, 0, 0, 13, 2.5, 16);
    private static final VoxelShape RIGHT_SN      = box(3, 0, 0, 13, 2.5, 16);

    // facing=east/west — LOWERED (y -= 8)
    private static final VoxelShape MIDDLE_EW_LOW  = box(0, -8, 3, 16, -5.5, 13);
    private static final VoxelShape LEFT_EW_LOW     = box(0, -8, 3, 16, -5.5, 13);
    private static final VoxelShape RIGHT_EW_LOW    = box(0, -8, 3, 16, -5.5, 13);

    // facing=south/north — LOWERED (y -= 8)
    private static final VoxelShape MIDDLE_SN_LOW   = box(3, -8, 0, 13, -5.5, 16);
    private static final VoxelShape LEFT_SN_LOW     = box(3, -8, 0, 13, -5.5, 16);
    private static final VoxelShape RIGHT_SN_LOW    = box(3, -8, 0, 13, -5.5, 16);

    public SpeedBumpBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.CANDLE).strength(1f, 10f)
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.EAST)
            .setValue(SHAPE, SpeedBumpShape.MIDDLE)
            .setValue(LOW, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE, LOW);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        BlockState state;
        if (direction.getAxis() == Direction.Axis.X) {
            state = this.defaultBlockState().setValue(FACING, Direction.SOUTH);
        } else {
            state = this.defaultBlockState().setValue(FACING, Direction.EAST);
        }
        
        boolean isLow = isHalfBlock(context.getLevel().getBlockState(context.getClickedPos().below()));
        return state.setValue(LOW, isLow);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean isLow = isHalfBlock(level.getBlockState(pos.below()));
        state = state.setValue(LOW, isLow);
        return getSpeedBumpShape(state, level, pos);
    }

    private static BlockState getSpeedBumpShape(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction leftDir, rightDir;

        if (facing == Direction.EAST || facing == Direction.WEST) {
            leftDir = Direction.WEST;
            rightDir = Direction.EAST;
        } else {
            leftDir = Direction.NORTH;
            rightDir = Direction.SOUTH;
        }

        boolean leftHas = hasSameSpeedBump(state, level, pos, leftDir);
        boolean rightHas = hasSameSpeedBump(state, level, pos, rightDir);

        if (rightHas && !leftHas) {
            return state.setValue(SHAPE, SpeedBumpShape.LEFT);
        }
        if (leftHas && !rightHas) {
            return state.setValue(SHAPE, SpeedBumpShape.RIGHT);
        }
        return state.setValue(SHAPE, SpeedBumpShape.MIDDLE);
    }

    private static boolean hasSameSpeedBump(BlockState state, LevelAccessor level, BlockPos pos, Direction direction) {
        BlockState neighbor = level.getBlockState(pos.relative(direction));
        return neighbor.getBlock() instanceof SpeedBumpBlock
            && neighbor.getValue(FACING) == state.getValue(FACING)
            && neighbor.getValue(LOW) == state.getValue(LOW);
    }

    /**
     * 判断下方方块是否为需要触发下降模型的半砖/楼梯。
     * 条件：注册名路径中包含 "slab" 或 "stair"，且是底部半砖/楼梯。
     */
    private static boolean isHalfBlock(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null) return false;
        String path = id.getPath();

        if (path.contains("slab") && state.getBlock() instanceof SlabBlock) {
            return state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        }
        if (path.contains("stair") && state.getBlock() instanceof StairBlock) {
            return state.getValue(StairBlock.HALF) == Half.BOTTOM;
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShapeForState(state);
    }

    private static VoxelShape getShapeForState(BlockState state) {
        Direction facing = state.getValue(FACING);
        SpeedBumpShape shape = state.getValue(SHAPE);
        boolean low = state.getValue(LOW);

        if (facing == Direction.EAST || facing == Direction.WEST) {
            if (low) {
                return switch (shape) {
                    case LEFT -> LEFT_EW_LOW;
                    case RIGHT -> RIGHT_EW_LOW;
                    default -> MIDDLE_EW_LOW;
                };
            } else {
                return switch (shape) {
                    case LEFT -> LEFT_EW;
                    case RIGHT -> RIGHT_EW;
                    default -> MIDDLE_EW;
                };
            }
        } else {
            if (low) {
                return switch (shape) {
                    case LEFT -> LEFT_SN_LOW;
                    case RIGHT -> RIGHT_SN_LOW;
                    default -> MIDDLE_SN_LOW;
                };
            } else {
                return switch (shape) {
                    case LEFT -> LEFT_SN;
                    case RIGHT -> RIGHT_SN;
                    default -> MIDDLE_SN;
                };
            }
        }
    }
}