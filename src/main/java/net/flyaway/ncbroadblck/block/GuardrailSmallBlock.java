package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.StringRepresentable;

public class GuardrailSmallBlock extends Block {

    public enum ConnectType implements StringRepresentable {
        NONE("none"), LEFT("left"), RIGHT("right"), MIDDLE("middle"), BOTH("both");
        private final String name;
        ConnectType(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final EnumProperty<ConnectType> CONNECT = EnumProperty.create("connect", ConnectType.class);
    public static final BooleanProperty LOW = BooleanProperty.create("low");

    private static final VoxelShape Z_NONE = Block.box(6, 0, 6, 10, 15, 10);
    private static final VoxelShape Z_LEFT = Block.box(0, 0, 6, 10, 15, 10);
    private static final VoxelShape Z_RIGHT = Block.box(6, 0, 6, 16, 15, 10);
    private static final VoxelShape Z_MIDDLE = Block.box(0, 0, 6, 16, 15, 10);
    private static final VoxelShape Z_BOTH = Block.box(0, 0, 6, 16, 15, 10);

    private static final VoxelShape X_NONE = Block.box(6, 0, 6, 10, 15, 10);
    private static final VoxelShape X_LEFT = Block.box(6, 0, 0, 10, 15, 10);
    private static final VoxelShape X_RIGHT = Block.box(6, 0, 6, 10, 15, 16);
    private static final VoxelShape X_MIDDLE = Block.box(6, 0, 0, 10, 15, 16);
    private static final VoxelShape X_BOTH = Block.box(6, 0, 0, 10, 15, 16);

    private static final VoxelShape Z_NONE_LOW = Block.box(6, -8, 6, 10, 7, 10);
    private static final VoxelShape Z_LEFT_LOW = Block.box(0, -8, 6, 10, 7, 10);
    private static final VoxelShape Z_RIGHT_LOW = Block.box(6, -8, 6, 16, 7, 10);
    private static final VoxelShape Z_MIDDLE_LOW = Block.box(0, -8, 6, 16, 7, 10);
    private static final VoxelShape Z_BOTH_LOW = Block.box(0, -8, 6, 16, 7, 10);

    private static final VoxelShape X_NONE_LOW = Block.box(6, -8, 6, 10, 7, 10);
    private static final VoxelShape X_LEFT_LOW = Block.box(6, -8, 0, 10, 7, 10);
    private static final VoxelShape X_RIGHT_LOW = Block.box(6, -8, 6, 10, 7, 16);
    private static final VoxelShape X_MIDDLE_LOW = Block.box(6, -8, 0, 10, 7, 16);
    private static final VoxelShape X_BOTH_LOW = Block.box(6, -8, 0, 10, 7, 16);

    public GuardrailSmallBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).sound(SoundType.COPPER).strength(2f, 10f).requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Z)
                .setValue(CONNECT, ConnectType.NONE)
                .setValue(LOW, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, CONNECT, LOW);
    }

    private boolean isLowSupport(LevelAccessor level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof SlabBlock && below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    private boolean isGuardrailConnect(BlockState state, Direction.Axis axis, boolean isLow, Direction checkDir) {
        if (!(state.getBlock() instanceof GuardrailBlock)) return false;
        if (state.getValue(GuardrailBlock.AXIS) != axis) return false;
        if (state.getValue(GuardrailBlock.LOW) != isLow) return false;
        if (!state.getValue(GuardrailBlock.SMALL)) return false;
        GuardrailBlock.ConnectType ct = state.getValue(GuardrailBlock.CONNECT);
        if (checkDir == Direction.WEST || checkDir == Direction.NORTH) {
            return ct == GuardrailBlock.ConnectType.LEFT;
        } else {
            return ct == GuardrailBlock.ConnectType.RIGHT;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getHorizontalDirection();
        Direction.Axis axis = (face == Direction.NORTH || face == Direction.SOUTH) ? Direction.Axis.Z : Direction.Axis.X;
        BlockState state = this.defaultBlockState().setValue(AXIS, axis).setValue(LOW, isLowSupport(context.getLevel(), context.getClickedPos()));
        return calculateState(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = state.setValue(LOW, isLowSupport(level, pos));
            newState = calculateState(newState, level, pos);
            if (newState != state) level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = state.setValue(LOW, isLowSupport(level, pos));
            newState = calculateState(newState, level, pos);
            if (newState != state) level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    private BlockState calculateState(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean isLow = state.getValue(LOW);
        Direction left, right;
        if (axis == Direction.Axis.Z) { left = Direction.WEST; right = Direction.EAST; }
        else { left = Direction.NORTH; right = Direction.SOUTH; }

        BlockPos leftPos = pos.relative(left);
        BlockPos rightPos = pos.relative(right);
        BlockState leftState = level.getBlockState(leftPos);
        BlockState rightState = level.getBlockState(rightPos);

        boolean leftConnected = (leftState.is(this) && leftState.getValue(AXIS) == axis && leftState.getValue(LOW) == isLow)
            || isGuardrailConnect(leftState, axis, isLow, left);
        boolean rightConnected = (rightState.is(this) && rightState.getValue(AXIS) == axis && rightState.getValue(LOW) == isLow)
            || isGuardrailConnect(rightState, axis, isLow, right);

        ConnectType current = state.getValue(CONNECT);
        ConnectType connectType;

        if (leftConnected && rightConnected) {
            if (current == ConnectType.MIDDLE || current == ConnectType.BOTH) return state;

            ConnectType leftConnect = leftState.is(this) ? leftState.getValue(CONNECT) : ConnectType.NONE;
            ConnectType rightConnect = rightState.is(this) ? rightState.getValue(CONNECT) : ConnectType.NONE;

            if (leftConnect == ConnectType.MIDDLE) connectType = ConnectType.BOTH;
            else if (leftConnect == ConnectType.BOTH) connectType = ConnectType.MIDDLE;
            else if (rightConnect == ConnectType.MIDDLE) connectType = ConnectType.BOTH;
            else if (rightConnect == ConnectType.BOTH) connectType = ConnectType.MIDDLE;
            else connectType = ConnectType.MIDDLE;
        } else if (leftConnected) {
            connectType = ConnectType.LEFT;
        } else if (rightConnected) {
            connectType = ConnectType.RIGHT;
        } else {
            connectType = ConnectType.NONE;
        }

        return state.setValue(CONNECT, connectType);
    }

    private VoxelShape getShapeForState(BlockState state) {
        Direction.Axis axis = state.getValue(AXIS);
        ConnectType connect = state.getValue(CONNECT);
        boolean isLow = state.getValue(LOW);
        if (axis == Direction.Axis.Z) {
            if (isLow) return switch (connect) {
                case NONE -> Z_NONE_LOW; case LEFT -> Z_LEFT_LOW; case RIGHT -> Z_RIGHT_LOW;
                case MIDDLE -> Z_MIDDLE_LOW; case BOTH -> Z_BOTH_LOW;
            };
            else return switch (connect) {
                case NONE -> Z_NONE; case LEFT -> Z_LEFT; case RIGHT -> Z_RIGHT;
                case MIDDLE -> Z_MIDDLE; case BOTH -> Z_BOTH;
            };
        } else {
            if (isLow) return switch (connect) {
                case NONE -> X_NONE_LOW; case LEFT -> X_LEFT_LOW; case RIGHT -> X_RIGHT_LOW;
                case MIDDLE -> X_MIDDLE_LOW; case BOTH -> X_BOTH_LOW;
            };
            else return switch (connect) {
                case NONE -> X_NONE; case LEFT -> X_LEFT; case RIGHT -> X_RIGHT;
                case MIDDLE -> X_MIDDLE; case BOTH -> X_BOTH;
            };
        }
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