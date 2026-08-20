package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;

public class AsphaltYellowGridlineSlabBlock extends SlabBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public AsphaltYellowGridlineSlabBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops());
    }

    protected AsphaltYellowGridlineSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return calculateState(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            BlockState newState = calculateState(state, level, pos);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockState newState = calculateState(state, level, pos);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    protected BlockState calculateState(BlockState state, Level level, BlockPos pos) {
        return state
                .setValue(NORTH, canConnect(state, level, pos.north()))
                .setValue(SOUTH, canConnect(state, level, pos.south()))
                .setValue(EAST,  canConnect(state, level, pos.east()))
                .setValue(WEST,  canConnect(state, level, pos.west()));
    }

    protected boolean canConnect(BlockState myState, Level level, BlockPos pos) {
        BlockState neighborState = level.getBlockState(pos);
        if (!(neighborState.getBlock() instanceof AsphaltYellowGridlineSlabBlock)) {
            return false;
        }
        return myState.getValue(BlockStateProperties.SLAB_TYPE) == neighborState.getValue(BlockStateProperties.SLAB_TYPE);
    }
}