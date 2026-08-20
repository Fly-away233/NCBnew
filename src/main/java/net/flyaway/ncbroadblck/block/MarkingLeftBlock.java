package net.flyaway.ncbroadblck.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MarkingLeftBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LOW = BooleanProperty.create("low");

    private ImmutableMap<BlockState, VoxelShape> shapes;

    public MarkingLeftBlock() {
        this(BlockBehaviour.Properties.of()
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops()
                .noCollission()
                .pushReaction(PushReaction.BLOCK)   // ← 防止被水冲掉
                .isRedstoneConductor((bs, br, bp) -> false));
    }

    protected MarkingLeftBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LOW, false));
    }

    /**
     * 检测下方是否为半砖：
     * 1. 若方块拥有 SLAB_TYPE 属性（三种状态），仅 BOTTOM 触发
     * 2. 若没有 SLAB_TYPE，但注册名包含 "_slab"，则直接触发
     */
    protected boolean isBottomSlab(BlockState state) {
        if (state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            return state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM;
        }

        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id != null && id.getPath().contains("_slab");
    }

    protected ImmutableMap<BlockState, VoxelShape> makeShapes() {
        return this.getShapeForEachState(state -> {
            Direction facing = state.getValue(FACING);
            boolean isLow = state.getValue(LOW);
            double y = isLow ? -8.0 : 0.0;

            return switch (facing) {
                case NORTH, SOUTH -> box(0, y, -4, 16, y + 0.05, 20);
                case EAST, WEST  -> box(-4, y, 0, 20, y + 0.05, 16);
                default          -> box(0, y, -4, 16, y + 0.05, 20);
            };
        });
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (this.shapes == null) {
            this.shapes = this.makeShapes();
        }
        return this.shapes.get(state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, LOW);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        boolean isLow = isBottomSlab(context.getLevel().getBlockState(pos.below()));

        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LOW, isLow);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (neighborPos.equals(pos.below())) {
            boolean shouldBeLow = isBottomSlab(level.getBlockState(neighborPos));
            if (state.getValue(LOW) != shouldBeLow) {
                level.setBlock(pos, state.setValue(LOW, shouldBeLow), Block.UPDATE_ALL);
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
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