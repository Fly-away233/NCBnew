package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

public class AsphaltWhiteLineBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty FRONT_LINK = BooleanProperty.create("front_link");
    public static final BooleanProperty BACK_LINK = BooleanProperty.create("back_link");

    public enum LinkType {
        NONE,
        NO_ROTATION,
        PERPENDICULAR
    }

    public AsphaltWhiteLineBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops());
    }

    protected AsphaltWhiteLineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FRONT_LINK, false)
                .setValue(BACK_LINK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, FRONT_LINK, BACK_LINK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
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
        if (!oldState.is(state.getBlock())) {
            updateLinkState(state, level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                    Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        updateLinkState(state, level, pos);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
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

        LinkType linkType = getLinkType(targetBlock, isFront);
        if (linkType == LinkType.NONE) return false;

        if (linkType == LinkType.NO_ROTATION) {
            return true;
        }

        Direction targetFacing = getFacing(targetState);
        if (targetFacing == null) return false;

        Direction currentFacing = state.getValue(FACING);

        return targetFacing.getAxis() != currentFacing.getAxis();
    }

    protected LinkType getLinkType(Block block, boolean isFront) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();

        if (path.equals("asphalt_white_link") || path.equals("asphalt_yellow_link")) {
            return LinkType.NO_ROTATION;
        }

        if (path.equals("asphalt_white_widelink") || path.equals("asphalt_yellow_widelink")) {
            return LinkType.PERPENDICULAR;
        }

        return LinkType.NONE;
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