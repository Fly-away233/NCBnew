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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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

public class Asphalt3ColorLineSlabBlock extends SlabBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty FRONT_LINK = BooleanProperty.create("front_link");
    public static final BooleanProperty BACK_LINK = BooleanProperty.create("back_link");

    public Asphalt3ColorLineSlabBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops());
    }

    protected Asphalt3ColorLineSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FRONT_LINK, false)
                .setValue(BACK_LINK, false)
                .setValue(TYPE, SlabType.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, FRONT_LINK, BACK_LINK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            state = this.defaultBlockState();
        }
        return state.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos,
                           BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) || oldState.getValue(TYPE) != state.getValue(TYPE)) {
            updateLinkState(state, level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        updateLinkState(state, level, pos);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (!level.isClientSide() && direction.getAxis().isHorizontal()) {
            level.scheduleTick(pos, this, 1);
        }
        return updated;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateLinkState(state, level, pos);
    }

    protected void updateLinkState(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);

        boolean frontLink = checkDirectionLink(state, level, pos, facing, true);
        boolean backLink  = checkDirectionLink(state, level, pos, facing.getOpposite(), false);

        BlockState newState = state
                .setValue(FRONT_LINK, frontLink)
                .setValue(BACK_LINK, backLink);

        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
        }
    }

    protected boolean checkDirectionLink(BlockState state, Level level, BlockPos pos,
                                         Direction direction, boolean isFront) {
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block targetBlock = targetState.getBlock();

        if (!isValidLinkBlock(targetBlock, isFront)) return false;

        SlabType currentType = state.getValue(TYPE);
        if (targetState.hasProperty(TYPE)) {
            SlabType targetType = targetState.getValue(TYPE);
            if (currentType != targetType) {
                return false;
            }
        } else {
            return false;
        }

        Direction targetFacing = getFacing(targetState);
        if (targetFacing == null) return false;

        Direction currentFacing = state.getValue(FACING);

        return targetFacing.getAxis() == currentFacing.getAxis();
    }

    protected boolean isValidLinkBlock(Block block, boolean isFront) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();
        return path.equals("asphalt_white_widelink_slab") || path.equals("asphalt_yellow_widelink_slab");
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