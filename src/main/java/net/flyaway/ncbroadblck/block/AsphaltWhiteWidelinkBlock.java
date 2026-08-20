package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

public class AsphaltWhiteWidelinkBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST);
    public static final BooleanProperty FRONT = BooleanProperty.create("front");
    public static final BooleanProperty BACK  = BooleanProperty.create("back");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty LEFT  = BooleanProperty.create("left");

    public AsphaltWhiteWidelinkBlock() {
        this(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .sound(SoundType.BASALT)
            .strength(1.5f, 10f)
            .requiresCorrectToolForDrops());
    }

    protected AsphaltWhiteWidelinkBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(FRONT, false)
            .setValue(BACK, false)
            .setValue(RIGHT, false)
            .setValue(LEFT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, FRONT, BACK, RIGHT, LEFT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction placedFacing = context.getHorizontalDirection().getOpposite();
        Direction facing = (placedFacing.getAxis() == Direction.Axis.Z) ? Direction.NORTH : Direction.EAST;
        BlockState state = this.defaultBlockState().setValue(FACING, facing);
        return calculateState(context.getLevel(), context.getClickedPos(), state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!oldState.is(state.getBlock())) {
            BlockState newState = calculateState(level, pos, state);
            if (newState != state) {
                level.setBlock(pos, newState, 2);
            }
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!state.is(newState.getBlock())) {
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockState newState = calculateState(level, pos, state);
        if (newState != state) {
            level.setBlock(pos, newState, 2);
        }
    }

    protected BlockState calculateState(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction front = facing;
        Direction back  = facing.getOpposite();
        Direction right = facing.getClockWise();
        Direction left  = facing.getCounterClockWise();

        return state
            .setValue(FRONT, checkFrontBack(level.getBlockState(pos.relative(front)), facing))
            .setValue(BACK,  checkFrontBack(level.getBlockState(pos.relative(back)), facing))
            .setValue(RIGHT, checkLeftRight(level.getBlockState(pos.relative(right)), facing))
            .setValue(LEFT,  checkLeftRight(level.getBlockState(pos.relative(left)), facing));
    }

    protected boolean checkFrontBack(BlockState targetState, Direction myFacing) {
        if (!isFrontLineType(targetState)) return false;
        Direction targetFacing = getFacing(targetState);
        return targetFacing != null && targetFacing.getAxis() == myFacing.getAxis();
    }

    protected boolean checkLeftRight(BlockState targetState, Direction myFacing) {
        if (!isRightLineType(targetState)) return false;
        Direction targetFacing = getFacing(targetState);
        return targetFacing != null && targetFacing.getAxis() != myFacing.getAxis();
    }

    protected boolean isFrontLineType(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.equals(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_3_color_line"))
            || id.equals(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_yellow_2_line"))
            || id.equals(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_yield_line"))
            || id.equals(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_white_wideline"));
    }

    protected boolean isRightLineType(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.equals(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_white_line"))
            || id.equals(ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_yellow_line"));
    }

    protected Direction getFacing(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals("facing") && prop instanceof DirectionProperty) {
                return (Direction) state.getValue(prop);
            }
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = state.getValue(FACING);
        Direction rotated = rotation.rotate(facing);
        if (rotated == Direction.SOUTH) rotated = Direction.NORTH;
        if (rotated == Direction.WEST)  rotated = Direction.EAST;
        return state.setValue(FACING, rotated);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}