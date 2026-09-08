package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GuardrailHighwayBlock extends Block {

    public enum ConnectType implements StringRepresentable {
        NONE("none"), LEFT("left"), RIGHT("right"), MIDDLE("middle"), BOTH("both");
        private final String name;
        ConnectType(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ConnectType> CONNECT = EnumProperty.create("connect", ConnectType.class);
    public static final BooleanProperty LOW = BooleanProperty.create("low");

    private static final VoxelShape BASE = Block.box(5, 0, 5, 11, 21.5, 11);
    private static final VoxelShape BASE_LOW = Block.box(5, -8, 5, 11, 13.5, 11);

    private static final VoxelShape RAIL_N = Block.box(0, 7.5, 1, 16, 21.5, 6);
    private static final VoxelShape RAIL_S = Block.box(0, 7.5, 10, 16, 21.5, 15);
    private static final VoxelShape RAIL_E = Block.box(10, 7.5, 0, 15, 21.5, 16);
    private static final VoxelShape RAIL_W = Block.box(1, 7.5, 0, 6, 21.5, 16);

    private static final VoxelShape RAIL_N_LOW = Block.box(0, -0.5, 1, 16, 13.5, 6);
    private static final VoxelShape RAIL_S_LOW = Block.box(0, -0.5, 10, 16, 13.5, 15);
    private static final VoxelShape RAIL_E_LOW = Block.box(10, -0.5, 0, 15, 13.5, 16);
    private static final VoxelShape RAIL_W_LOW = Block.box(1, -0.5, 0, 6, 13.5, 16);

    private static final VoxelShape SHAPE_N = Shapes.or(BASE, RAIL_N);
    private static final VoxelShape SHAPE_S = Shapes.or(BASE, RAIL_S);
    private static final VoxelShape SHAPE_E = Shapes.or(BASE, RAIL_E);
    private static final VoxelShape SHAPE_W = Shapes.or(BASE, RAIL_W);
    private static final VoxelShape SHAPE_N_LOW = Shapes.or(BASE_LOW, RAIL_N_LOW);
    private static final VoxelShape SHAPE_S_LOW = Shapes.or(BASE_LOW, RAIL_S_LOW);
    private static final VoxelShape SHAPE_E_LOW = Shapes.or(BASE_LOW, RAIL_E_LOW);
    private static final VoxelShape SHAPE_W_LOW = Shapes.or(BASE_LOW, RAIL_W_LOW);

    public GuardrailHighwayBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).sound(SoundType.COPPER).strength(2f, 10f).requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CONNECT, ConnectType.NONE)
                .setValue(LOW, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CONNECT, LOW);
    }

    private boolean isLowSupport(LevelAccessor level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof SlabBlock && below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = this.defaultBlockState().setValue(FACING, facing).setValue(LOW, isLowSupport(context.getLevel(), context.getClickedPos()));
        return calculateState(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = calculateState(state, level, pos);
            if (newState != state) level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = calculateState(state, level, pos);
            if (newState != state) level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    private BlockState calculateState(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        boolean isLow = isLowSupport(level, pos);
        Direction left, right;
        switch (facing) {
            case NORTH -> { left = Direction.WEST; right = Direction.EAST; }
            case SOUTH -> { left = Direction.EAST; right = Direction.WEST; }
            case EAST  -> { left = Direction.NORTH; right = Direction.SOUTH; }
            default    -> { left = Direction.SOUTH; right = Direction.NORTH; }
        }

        BlockPos leftPos = pos.relative(left);
        BlockPos rightPos = pos.relative(right);
        BlockState leftState = level.getBlockState(leftPos);
        BlockState rightState = level.getBlockState(rightPos);

        // 连接要求：相邻方块是同一种、朝向相同、low 状态一致
        boolean leftConnected = leftState.is(this) && leftState.getValue(FACING) == facing && leftState.getValue(LOW) == isLow;
        boolean rightConnected = rightState.is(this) && rightState.getValue(FACING) == facing && rightState.getValue(LOW) == isLow;

        ConnectType current = state.getValue(CONNECT);
        ConnectType connectType;

        if (leftConnected && rightConnected) {
            if (current == ConnectType.MIDDLE || current == ConnectType.BOTH) {
                return state.setValue(LOW, isLow);
            }
            ConnectType lc = leftState.getValue(CONNECT);
            ConnectType rc = rightState.getValue(CONNECT);
            if (lc == ConnectType.MIDDLE) connectType = ConnectType.BOTH;
            else if (lc == ConnectType.BOTH) connectType = ConnectType.MIDDLE;
            else if (rc == ConnectType.MIDDLE) connectType = ConnectType.BOTH;
            else if (rc == ConnectType.BOTH) connectType = ConnectType.MIDDLE;
            else connectType = ConnectType.MIDDLE;
        } else if (leftConnected) {
            connectType = ConnectType.LEFT;
        } else if (rightConnected) {
            connectType = ConnectType.RIGHT;
        } else {
            connectType = ConnectType.NONE;
        }

        return state.setValue(CONNECT, connectType).setValue(LOW, isLow);
    }

    private VoxelShape getShapeForState(BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean isLow = state.getValue(LOW);
        boolean isMiddle = state.getValue(CONNECT) == ConnectType.MIDDLE;

        VoxelShape shape = switch (facing) {
            case NORTH -> isLow ? SHAPE_N_LOW : SHAPE_N;
            case SOUTH -> isLow ? SHAPE_S_LOW : SHAPE_S;
            case EAST  -> isLow ? SHAPE_E_LOW : SHAPE_E;
            default    -> isLow ? SHAPE_W_LOW : SHAPE_W;
        };
        if (isMiddle) {
            shape = switch (facing) {
                case NORTH -> isLow ? RAIL_N_LOW : RAIL_N;
                case SOUTH -> isLow ? RAIL_S_LOW : RAIL_S;
                case EAST  -> isLow ? RAIL_E_LOW : RAIL_E;
                default    -> isLow ? RAIL_W_LOW : RAIL_W;
            };
        }
        return shape;
    }

    @Override public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getShapeForState(s); }
    @Override public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getShapeForState(s); }
    @Override public VoxelShape getOcclusionShape(BlockState s, BlockGetter l, BlockPos p) { return getShapeForState(s); }
    @Override public VoxelShape getBlockSupportShape(BlockState s, BlockGetter l, BlockPos p) { return getShapeForState(s); }
    @Override public VoxelShape getInteractionShape(BlockState s, BlockGetter l, BlockPos p) { return getShapeForState(s); }
    @Override public VoxelShape getVisualShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return getShapeForState(s); }
    @Override public int getLightBlock(BlockState s, BlockGetter l, BlockPos p) { return 0; }
    @Override public boolean propagatesSkylightDown(BlockState s, BlockGetter r, BlockPos p) { return true; }
    @Override public boolean useShapeForLightOcclusion(BlockState s) { return true; }
}