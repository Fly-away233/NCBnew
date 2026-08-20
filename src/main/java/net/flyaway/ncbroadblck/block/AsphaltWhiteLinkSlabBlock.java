package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;

import java.util.Set;

public class AsphaltWhiteLinkSlabBlock extends SlabBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST  = BooleanProperty.create("east");
    public static final BooleanProperty WEST  = BooleanProperty.create("west");

    protected final Set<ResourceLocation> connectableBlocks;

    protected static final Set<ResourceLocation> DEFAULT_CONNECTABLES = Set.of(
        ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_white_line_slab"),
        ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_yellow_line_slab")
    );

    public AsphaltWhiteLinkSlabBlock() {
        this(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .sound(SoundType.BASALT)
            .strength(1.5f, 10f)
            .requiresCorrectToolForDrops());
    }

    protected AsphaltWhiteLinkSlabBlock(BlockBehaviour.Properties properties) {
        this(properties, DEFAULT_CONNECTABLES);
    }

    protected AsphaltWhiteLinkSlabBlock(BlockBehaviour.Properties properties, Set<ResourceLocation> connectableBlocks) {
        super(properties);
        this.connectableBlocks = connectableBlocks;

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(EAST,  false)
            .setValue(WEST,  false)
            .setValue(TYPE, SlabType.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            state = this.defaultBlockState();
        }

        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return state
            .setValue(NORTH, isConnectable(level, pos.north(), Direction.NORTH, state))
            .setValue(SOUTH, isConnectable(level, pos.south(), Direction.SOUTH, state))
            .setValue(EAST,  isConnectable(level, pos.east(),  Direction.EAST,  state))
            .setValue(WEST,  isConnectable(level, pos.west(),  Direction.WEST,  state));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!oldState.is(state.getBlock()) || oldState.getValue(TYPE) != state.getValue(TYPE)) {
            BlockState newState = calculateState(level, pos, state);
            if (newState != state) {
                level.setBlock(pos, newState, 2);
            }
        }
        level.updateNeighborsAt(pos, this);
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

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (!direction.getAxis().isHorizontal()) {
            return updated;
        }
        boolean connected = isConnectable(neighborState, direction, updated);
        BlockState result = switch (direction) {
            case NORTH -> updated.setValue(NORTH, connected);
            case SOUTH -> updated.setValue(SOUTH, connected);
            case EAST  -> updated.setValue(EAST,  connected);
            case WEST  -> updated.setValue(WEST,  connected);
            default    -> updated;
        };
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
        return result;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState newState = calculateState(level, pos, state);
        if (newState != state) {
            level.setBlock(pos, newState, 2);
        }
    }

    protected BlockState calculateState(LevelAccessor level, BlockPos pos, BlockState state) {
        return state
            .setValue(NORTH, isConnectable(level, pos.north(), Direction.NORTH, state))
            .setValue(SOUTH, isConnectable(level, pos.south(), Direction.SOUTH, state))
            .setValue(EAST,  isConnectable(level, pos.east(),  Direction.EAST,  state))
            .setValue(WEST,  isConnectable(level, pos.west(),  Direction.WEST,  state));
    }

    protected boolean isConnectable(LevelAccessor level, BlockPos pos, Direction connectionDirection, BlockState currentState) {
        return isConnectable(level.getBlockState(pos), connectionDirection, currentState);
    }

    protected boolean isConnectable(BlockState neighborState, Direction connectionDirection, BlockState currentState) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(neighborState.getBlock());
        if (!connectableBlocks.contains(id)) return false;

        if (neighborState.hasProperty(TYPE)) {
            SlabType neighborType = neighborState.getValue(TYPE);
            SlabType currentType = currentState.getValue(TYPE);
            if (neighborType != currentType) {
                return false;
            }
        } else {
            return false;
        }

        Direction facing = getFacing(neighborState);
        if (facing == null) return true;

        return facing.getAxis() == connectionDirection.getAxis();
    }

    protected Direction getFacing(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals("facing") && prop instanceof DirectionProperty) {
                return (Direction) state.getValue(prop);
            }
        }
        return null;
    }
}