package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.Level;
import net.minecraft.util.StringRepresentable;

public class BollardSidewalkBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LOWERED = BooleanProperty.create("lowered");
    public static final BooleanProperty STACKED = BooleanProperty.create("stacked");
    public static final BooleanProperty HAS_PLATE = BooleanProperty.create("has_plate");

    public enum PlateFacing implements StringRepresentable {
        NONE("none"),
        NORTH("north"),
        EAST("east"),
        SOUTH("south"),
        WEST("west");

        private final String name;
        PlateFacing(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public static final EnumProperty<PlateFacing> PLATE_FACING = EnumProperty.create("plate_facing", PlateFacing.class);

    private static final VoxelShape SHAPE = box(6, 0, 6, 10, 20, 10);
    private static final VoxelShape LOWERED_SHAPE = box(6, -8, 6, 10, 12, 10);
    private static final VoxelShape PLATE_SHAPE_NS = box(4, -9, 0, 12, 0, 16);
    private static final VoxelShape PLATE_SHAPE_EW = box(0, -9, 4, 16, 0, 12);

    public BollardSidewalkBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).sound(SoundType.COPPER).strength(2f, 10f).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(WATERLOGGED, false)
            .setValue(LOWERED, false)
            .setValue(STACKED, false)
            .setValue(HAS_PLATE, false)
            .setValue(PLATE_FACING, PlateFacing.NONE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape baseShape = state.getValue(LOWERED) ? LOWERED_SHAPE : SHAPE;
        if (state.getValue(HAS_PLATE)) {
            Direction plateDir = getPlateDirection(state);
            VoxelShape plateShape = getPlateShape(plateDir);
            return Shapes.or(baseShape, plateShape);
        }
        return baseShape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return propagatesSkylightDown(state, worldIn, pos) ? 0 : 1;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED, LOWERED, STACKED, HAS_PLATE, PLATE_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = super.getStateForPlacement(context).setValue(WATERLOGGED, flag);
        BlockState belowState = context.getLevel().getBlockState(context.getClickedPos().below());
        return calculateState(state, belowState);
    }

    private BlockState calculateState(BlockState state, BlockState belowState) {
        boolean isStacked = belowState.is(this);
        boolean isLowered = !isStacked && isSlabBlock(belowState) && !isYellow2LineSlab(belowState);
        boolean hasPlate = !isStacked && isYellow2LineSlab(belowState);

        PlateFacing plateFacing = PlateFacing.NONE;
        if (hasPlate && belowState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            plateFacing = switch (belowState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case NORTH -> PlateFacing.NORTH;
                case EAST -> PlateFacing.EAST;
                case SOUTH -> PlateFacing.SOUTH;
                case WEST -> PlateFacing.WEST;
                default -> PlateFacing.NORTH;
            };
        }

        return state.setValue(STACKED, isStacked)
                    .setValue(LOWERED, isLowered)
                    .setValue(HAS_PLATE, hasPlate)
                    .setValue(PLATE_FACING, plateFacing);
    }

    private boolean isYellow2LineSlab(BlockState state) {
        if (!(state.getBlock() instanceof SlabBlock)) return false;
        if (!state.hasProperty(SlabBlock.TYPE)) return false;
        if (state.getValue(SlabBlock.TYPE) != SlabType.BOTTOM) return false;
        String id = state.getBlock().getDescriptionId();
        return id.contains("asphalt_yellow_2_line_slab");
    }

    private Direction getPlateDirection(BlockState state) {
        return switch (state.getValue(PLATE_FACING)) {
            case NORTH -> Direction.NORTH;
            case EAST -> Direction.EAST;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    private VoxelShape getPlateShape(Direction dir) {
        return switch (dir) {
            case NORTH, SOUTH -> PLATE_SHAPE_NS;
            case EAST, WEST -> PLATE_SHAPE_EW;
            default -> PLATE_SHAPE_NS;
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, 
                                BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide() && neighborPos.equals(pos.below())) {
            BlockState belowState = level.getBlockState(pos.below());
            BlockState newState = calculateState(state, belowState);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && !oldState.is(this)) {
            BlockState belowState = level.getBlockState(pos.below());
            BlockState newState = calculateState(state, belowState);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }
    }

    private boolean isSlabBlock(BlockState state) {
        return state.getBlock() instanceof SlabBlock 
            && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }
}