package net.flyaway.ncbroadblck.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficSignalSmallpoleBlock extends Block {

    public enum PoleType implements StringRepresentable {
        SINGLE("single"),
        LOW("low"),
        STACKED("stacked");

        private final String name;

        PoleType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<PoleType> POLE_TYPE = EnumProperty.create("pole_type", PoleType.class);

    private static final VoxelShape SHAPE = Shapes.box(5.0 / 16.0, 0.0, 5.0 / 16.0, 11.0 / 16.0, 1.0, 11.0 / 16.0);

    public TrafficSignalSmallpoleBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .sound(SoundType.COPPER)
            .strength(2f, 10f)
            .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(POLE_TYPE, PoleType.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POLE_TYPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POLE_TYPE, getPoleType(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (level.isClientSide) return;

        PoleType current = state.getValue(POLE_TYPE);
        PoleType expected = getPoleType(level, pos);

        if (current != expected) {
            level.setBlock(pos, state.setValue(POLE_TYPE, expected), 3);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(this)) {
            PoleType type = getPoleType(level, pos);
            BlockState correct = state.setValue(POLE_TYPE, type);
            if (correct != state) {
                level.setBlock(pos, correct, 2);
            }
            level.updateNeighborsAt(pos.above(), this);
            level.updateNeighborsAt(pos.below(), this);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        // 非路牌物品：交给默认流程，确保音效正常
        if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)
            || !(blockItem.getBlock() instanceof RoadsignBlock)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 最底端判断：下方必须有同类杆或同类型路牌支撑
        BlockState belowState = level.getBlockState(pos.below());
        Block belowBlock = belowState.getBlock();
        boolean hasSupport = belowBlock == this
            || (belowBlock instanceof RoadsignBlock rb
                && rb.getBaseType(belowState) == RoadsignBlock.BaseType.SMALLPOLE);

        if (!hasSupport) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int rotation = RoadsignBlock.playerYawToRotation(player.getYRot());
        BlockState signState = ((RoadsignBlock) blockItem.getBlock()).defaultBlockState()
            .setValue(RoadsignBlock.BASE, RoadsignBlock.BaseType.SMALLPOLE)
            .setValue(RoadsignBlock.ROTATION, rotation);

        level.setBlock(pos, signState, 3);
        level.playSound(null, pos, signState.getSoundType().getPlaceSound(),
            net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return ItemInteractionResult.SUCCESS;
    }

    // 改为包级私有，供 RoadsignBlock.onRemove 调用
    PoleType getPoleType(BlockGetter level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        Block belowBlock = belowState.getBlock();

        if (belowBlock == this) {
            return PoleType.STACKED;
        }

        if (belowBlock instanceof RoadsignBlock roadsign) {
            if (roadsign.getBaseType(belowState) == RoadsignBlock.BaseType.SMALLPOLE) {
                return PoleType.STACKED;
            }
        }

        if (belowBlock instanceof SlabBlock) {
            if (belowState.hasProperty(BlockStateProperties.SLAB_TYPE)
                && belowState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM) {
                return PoleType.LOW;
            }
        }

        return PoleType.SINGLE;
    }
}