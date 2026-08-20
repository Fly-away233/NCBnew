package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficSignalPoleBlock extends Block {
    public static final EnumProperty<ConnectionType> CONNECTION = EnumProperty.create("connection", ConnectionType.class);
    public static final EnumProperty<LowType> LOW = EnumProperty.create("low", LowType.class);

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 16, 13);

    public TrafficSignalPoleBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .sound(SoundType.COPPER)
            .strength(2f, 10f)
            .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(CONNECTION, ConnectionType.NONE)
            .setValue(LOW, LowType.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTION, LOW);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return calculateState(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return calculateState(level, pos);
        }
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(this)) {
            BlockState correct = calculateState(level, pos);
            if (correct != state) {
                level.setBlock(pos, correct, 2);
            }
            level.updateNeighborsAt(pos.above(), this);
            level.updateNeighborsAt(pos.below(), this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!newState.is(this)) {
            level.updateNeighborsAt(pos.above(), this);
            level.updateNeighborsAt(pos.below(), this);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        // 非路牌物品：交给默认流程，确保音效正常
        if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)
            || !(blockItem.getBlock() instanceof RoadsignBlock)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 最底端判断：下方必须有同类杆或同类型路牌支撑
        BlockState belowState = level.getBlockState(pos.below());
        Block belowBlock = belowState.getBlock();
        boolean hasSupport = belowBlock == this
            || (belowBlock instanceof RoadsignBlock rb
                && rb.getBaseType(belowState) == RoadsignBlock.BaseType.POLE);

        if (!hasSupport) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int rotation = RoadsignBlock.playerYawToRotation(player.getYRot());
        BlockState signState = ((RoadsignBlock) blockItem.getBlock()).defaultBlockState()
            .setValue(RoadsignBlock.BASE, RoadsignBlock.BaseType.POLE)
            .setValue(RoadsignBlock.ROTATION, rotation);

        level.setBlock(pos, signState, 3);
        level.playSound(null, pos, signState.getSoundType().getPlaceSound(),
            net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return ItemInteractionResult.SUCCESS;
    }

    // 改为包级私有，供 RoadsignBlock.onRemove 调用
    BlockState calculateState(LevelAccessor level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();

        boolean hasAbove = level.getBlockState(above).is(this) || isRoadsignOnPole(level, above);
        boolean hasBelow = level.getBlockState(below).is(this) || isRoadsignOnPole(level, below);

        ConnectionType connection;
        if (hasAbove && hasBelow) {
            connection = ConnectionType.BOTH;
        } else if (hasAbove) {
            connection = ConnectionType.UP;
        } else if (hasBelow) {
            connection = ConnectionType.DOWN;
        } else {
            connection = ConnectionType.NONE;
        }

        LowType low = LowType.FALSE;
        if (connection == ConnectionType.NONE || connection == ConnectionType.UP) {
            BlockState belowState = level.getBlockState(below);
            if (belowState.getBlock() instanceof SlabBlock) {
                if (belowState.hasProperty(BlockStateProperties.SLAB_TYPE)
                    && belowState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM) {
                    low = LowType.TRUE;
                }
            }
        }

        return this.defaultBlockState()
            .setValue(CONNECTION, connection)
            .setValue(LOW, low);
    }

    private boolean isRoadsignOnPole(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RoadsignBlock) {
            return state.getValue(RoadsignBlock.BASE) == RoadsignBlock.BaseType.POLE;
        }
        return false;
    }

    public enum ConnectionType implements StringRepresentable {
        NONE("none"), UP("up"), BOTH("both"), DOWN("down");
        private final String name;
        ConnectionType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum LowType implements StringRepresentable {
        FALSE("false"), TRUE("true");
        private final String name;
        LowType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}