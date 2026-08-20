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

import java.util.EnumMap;
import java.util.Map;

public class AsphaltRoadSlopeBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 2);

    private static final Map<Direction, VoxelShape[]> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            SHAPES.put(dir, new VoxelShape[]{
                    Shapes.empty(),
                    makeSlopeShape(dir, 0),
                    makeSlopeShape(dir, 1)
            });
        }
    }

    private static VoxelShape makeSlopeShape(Direction facing, int layerOffset) {
        int baseY = layerOffset * 8;
        Direction actual = facing.getOpposite();

        VoxelShape lower = switch (actual) {
            case NORTH -> box(0, baseY, 8, 16, baseY + 4, 16);
            case SOUTH -> box(0, baseY, 0, 16, baseY + 4, 8);
            case EAST  -> box(0, baseY, 0, 8, baseY + 4, 16);
            case WEST  -> box(8, baseY, 0, 16, baseY + 4, 16);
            default -> Shapes.empty();
        };

        VoxelShape upper = switch (actual) {
            case NORTH -> box(0, baseY + 4, 0, 16, baseY + 8, 8);
            case SOUTH -> box(0, baseY + 4, 8, 16, baseY + 8, 16);
            case EAST  -> box(8, baseY + 4, 0, 16, baseY + 8, 16);
            case WEST  -> box(0, baseY + 4, 0, 8, baseY + 8, 16);
            default -> Shapes.empty();
        };

        return Shapes.or(lower, upper);
    }

    /** 供子类传入自定义 Properties */
    protected AsphaltRoadSlopeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LAYERS, 1));
    }

    /** 无参构造：供 MCreator 注册基类时使用 */
    public AsphaltRoadSlopeBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops()
                .noOcclusion()); // ← 全透光：不遮挡相邻方块的面
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
        if (clickedState.getBlock() instanceof AsphaltRoadSlopeBlock
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
            int current = state.getValue(LAYERS);
            int next = (current == 1) ? 2 : 1;

            if (!level.isClientSide) {
                BlockState newState = state.setValue(LAYERS, next);
                level.setBlock(pos, newState, Block.UPDATE_ALL);

                SoundType soundtype = this.soundType;
                level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(LAYERS)];
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