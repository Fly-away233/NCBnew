package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AsphaltRoadPlateBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);

    private static final VoxelShape[] SHAPES = new VoxelShape[9];

    static {
        for (int i = 1; i <= 8; i++) {
            SHAPES[i] = box(0, 0, 0, 16, i * 2, 16);
        }
    }

    public AsphaltRoadPlateBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isRedstoneConductor((bs, br, bp) -> false));
    }

    protected AsphaltRoadPlateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LAYERS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, LAYERS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        Direction face = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos().relative(face.getOpposite());
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);

        int layers = 1;
        if (clickedState.getBlock() instanceof AsphaltRoadPlateBlock
                && face != Direction.UP && face != Direction.DOWN) {
            layers = clickedState.getValue(LAYERS);
        }

        return state.setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(LAYERS, layers);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.isEmpty() && stack.is(this.asItem())
                && hitResult.getDirection() == Direction.UP) {
            int currentLayers = state.getValue(LAYERS);
            if (currentLayers < 8) {
                if (!level.isClientSide) {
                    BlockState newState = state.setValue(LAYERS, currentLayers + 1);
                    level.setBlock(pos, newState, Block.UPDATE_ALL);

                    SoundType soundtype = this.soundType;
                    level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                            (soundtype.getVolume() + 1.0F) / 2.0F,
                            soundtype.getPitch() * 0.8F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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