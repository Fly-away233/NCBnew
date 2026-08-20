package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;

public class AsphaltYellowGridlineBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public AsphaltYellowGridlineBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops());
    }

    protected AsphaltYellowGridlineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return calculateState(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
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
                .setValue(NORTH, canConnect(level, pos.north()))
                .setValue(SOUTH, canConnect(level, pos.south()))
                .setValue(EAST,  canConnect(level, pos.east()))
                .setValue(WEST,  canConnect(level, pos.west()));
    }

    protected boolean canConnect(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof AsphaltYellowGridlineBlock;
    }
}