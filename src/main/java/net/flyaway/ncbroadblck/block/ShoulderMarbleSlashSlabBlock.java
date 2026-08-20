package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.flyaway.ncbroadblck.block.entity.ShoulderMarbleSlashSlabBlockEntity;
import net.flyaway.ncbroadblck.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class ShoulderMarbleSlashSlabBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape BASE = box(0, 0, 0, 16, 8.5, 16);

    private static final VoxelShape N_R1 = box(0, 8.5, 12, 4, 16, 16);
    private static final VoxelShape N_R2 = box(4, 8.5, 8, 8, 16, 16);
    private static final VoxelShape N_R3 = box(8, 8.5, 4, 12, 16, 16);
    private static final VoxelShape N_R4 = box(12, 8.5, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_N = Shapes.or(BASE, N_R1, N_R2, N_R3, N_R4);

    private static final VoxelShape E_R1 = box(0, 8.5, 0, 4, 16, 4);
    private static final VoxelShape E_R2 = box(0, 8.5, 4, 8, 16, 8);
    private static final VoxelShape E_R3 = box(0, 8.5, 8, 12, 16, 12);
    private static final VoxelShape E_R4 = box(0, 8.5, 12, 16, 16, 16);
    private static final VoxelShape SHAPE_E = Shapes.or(BASE, E_R1, E_R2, E_R3, E_R4);

    private static final VoxelShape S_R1 = box(12, 8.5, 0, 16, 16, 4);
    private static final VoxelShape S_R2 = box(8, 8.5, 0, 12, 16, 8);
    private static final VoxelShape S_R3 = box(4, 8.5, 0, 8, 16, 12);
    private static final VoxelShape S_R4 = box(0, 8.5, 0, 4, 16, 16);
    private static final VoxelShape SHAPE_S = Shapes.or(BASE, S_R1, S_R2, S_R3, S_R4);

    private static final VoxelShape W_R1 = box(12, 8.5, 12, 16, 16, 16);
    private static final VoxelShape W_R2 = box(8, 8.5, 8, 16, 16, 12);
    private static final VoxelShape W_R3 = box(4, 8.5, 4, 16, 16, 8);
    private static final VoxelShape W_R4 = box(0, 8.5, 0, 16, 16, 4);
    private static final VoxelShape SHAPE_W = Shapes.or(BASE, W_R1, W_R2, W_R3, W_R4);

    public ShoulderMarbleSlashSlabBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                .sound(SoundType.STONE)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_N;
            case EAST  -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST  -> SHAPE_W;
            default    -> SHAPE_N;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShoulderMarbleSlashSlabBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() && type == ModBlockEntities.SHOULDER_MARBLE_SLASH_SLAB.get()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<ShoulderMarbleSlashSlabBlockEntity>) ShoulderMarbleSlashSlabBlockEntity::clientTick
                : null;
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