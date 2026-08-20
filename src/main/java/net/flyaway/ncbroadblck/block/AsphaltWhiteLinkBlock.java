package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;

import java.util.Set;

public class AsphaltWhiteLinkBlock extends Block {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST  = BooleanProperty.create("east");
    public static final BooleanProperty WEST  = BooleanProperty.create("west");

    protected final Set<ResourceLocation> connectableBlocks;

    protected static final Set<ResourceLocation> DEFAULT_CONNECTABLES = Set.of(
        ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_white_line"),
        ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "asphalt_yellow_line")
    );

    public AsphaltWhiteLinkBlock() {
        this(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .sound(SoundType.BASALT)
            .strength(1.5f, 10f)
            .requiresCorrectToolForDrops());
    }

    protected AsphaltWhiteLinkBlock(BlockBehaviour.Properties properties) {
        this(properties, DEFAULT_CONNECTABLES);
    }

    protected AsphaltWhiteLinkBlock(BlockBehaviour.Properties properties, Set<ResourceLocation> connectableBlocks) {
        super(properties);
        this.connectableBlocks = connectableBlocks;

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(EAST,  false)
            .setValue(WEST,  false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
            .setValue(NORTH, isConnectable(level, pos.north(), Direction.NORTH))
            .setValue(SOUTH, isConnectable(level, pos.south(), Direction.SOUTH))
            .setValue(EAST,  isConnectable(level, pos.east(),  Direction.EAST))
            .setValue(WEST,  isConnectable(level, pos.west(),  Direction.WEST));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!direction.getAxis().isHorizontal()) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }

        boolean connected = isConnectable(neighborState, direction);
        return switch (direction) {
            case NORTH -> state.setValue(NORTH, connected);
            case SOUTH -> state.setValue(SOUTH, connected);
            case EAST  -> state.setValue(EAST,  connected);
            case WEST  -> state.setValue(WEST,  connected);
            default    -> super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        };
    }

    protected boolean isConnectable(LevelAccessor level, BlockPos pos, Direction connectionDirection) {
        return isConnectable(level.getBlockState(pos), connectionDirection);
    }

    protected boolean isConnectable(BlockState state, Direction connectionDirection) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!connectableBlocks.contains(id)) return false;

        Direction facing = getFacing(state);
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