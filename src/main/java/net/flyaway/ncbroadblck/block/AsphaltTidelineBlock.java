package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

public class AsphaltTidelineBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public enum FrontState implements StringRepresentable {
        NONE("none"),
        SAME("same"),
        OPPOSITE("opposite");

        private final String name;

        FrontState(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<FrontState> FRONT_STATE = EnumProperty.create("front_state", FrontState.class);

    public AsphaltTidelineBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops());
    }

    protected AsphaltTidelineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FRONT_STATE, FrontState.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, FRONT_STATE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && !oldState.is(this)) {
            updateFrontState(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            if (neighborPos.equals(pos.relative(facing))) {
                BlockState newState = calculateFrontState(state, level, pos);
                if (!newState.equals(state)) {
                    level.setBlock(pos, newState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private BlockState calculateFrontState(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos frontPos = pos.relative(facing);
        BlockState frontState = level.getBlockState(frontPos);

        if (frontState.is(this)) {
            FrontState linkedState = frontState.getValue(FRONT_STATE);
            return state.setValue(FRONT_STATE, linkedState);
        }

        String id = frontState.getBlock().getDescriptionId();
        boolean isWhite1Line = id.contains("asphalt_white_line") || id.contains("asphaltwhiteline");

        if (!isWhite1Line) {
            return state.setValue(FRONT_STATE, FrontState.NONE);
        }

        if (frontState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction frontFacing = frontState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (frontFacing == facing) {
                return state.setValue(FRONT_STATE, FrontState.SAME);
            } else if (frontFacing == facing.getOpposite()) {
                return state.setValue(FRONT_STATE, FrontState.OPPOSITE);
            }
        }

        return state.setValue(FRONT_STATE, FrontState.NONE);
    }

    private void updateFrontState(Level level, BlockPos pos, BlockState state) {
        BlockState newState = calculateFrontState(state, level, pos);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
}