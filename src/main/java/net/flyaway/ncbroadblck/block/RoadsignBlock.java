package net.flyaway.ncbroadblck.block;

import net.flyaway.ncbroadblck.init.NcbRoadblckModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class RoadsignBlock extends Block {

    public enum BaseType implements StringRepresentable {
        NONE("none"),
        POLE("pole"),
        SMALLPOLE("smallpole");

        private final String name;

        BaseType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 1, 2);
    public static final EnumProperty<BaseType> BASE = EnumProperty.create("base", BaseType.class);
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape SHAPE_WEST  = Block.box(15, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 0, 0, 1, 16, 16);

    public RoadsignBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(STAGE, 1)
            .setValue(BASE, BaseType.NONE)
            .setValue(ROTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, BASE, ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis() == Direction.Axis.Y) {
            return null;
        }

        Direction signFacing = face.getOpposite();
        int rotation = switch (signFacing) {
            case SOUTH -> 0;
            case WEST  -> 4;
            case NORTH -> 8;
            case EAST  -> 12;
            default    -> 0;
        };

        return this.defaultBlockState()
            .setValue(BASE, BaseType.NONE)
            .setValue(STAGE, 1)
            .setValue(ROTATION, rotation);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (blockItem.getBlock() != this) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BaseType base = state.getValue(BASE);
        int stage = state.getValue(STAGE);

        if (base != BaseType.NONE && stage < 2) {
            level.setBlock(pos, state.setValue(STAGE, 2), 3);
            level.playSound(null, pos, state.getSoundType().getPlaceSound(),
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;

        BaseType base = state.getValue(BASE);

        if (base == BaseType.SMALLPOLE) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            Block belowBlock = belowState.getBlock();

            boolean valid = belowBlock instanceof RoadsignBlock || belowBlock instanceof TrafficSignalSmallpoleBlock;
            if (!valid && belowBlock instanceof SlabBlock) {
                if (belowState.hasProperty(BlockStateProperties.SLAB_TYPE)
                    && belowState.getValue(BlockStateProperties.SLAB_TYPE) == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM) {
                    valid = true;
                }
            }
            if (!valid) {
                level.destroyBlock(pos, true);
            }
        } else if (base == BaseType.POLE) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            Block belowBlock = belowState.getBlock();
            boolean valid = belowBlock instanceof RoadsignBlock || belowBlock instanceof TrafficSignalPoleBlock;
            if (!valid) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (!level.isClientSide) {
            // 统计和饥饿
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);

            // 生存模式：手动生成掉落物
            if (!player.isCreative()) {
                int stage = state.getValue(STAGE);
                popResource(level, pos, new ItemStack(this.asItem(), stage));
            }

            // 设置方块为空气，触发 onRemove 复原杆
            level.setBlock(pos, fluid.createLegacyBlock(), 3);
        }
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        // 空实现：所有逻辑在 onDestroyedByPlayer 中处理
        // 避免默认的 dropResources 再次调用 getDrops 导致重复掉落
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (level.isClientSide || isMoving || !newState.isAir() || state.getBlock() != this) {
            return;
        }

        BaseType base = state.getValue(BASE);
        if (base == BaseType.NONE) {
            return;
        }

        // 只复原杆方块，不掉落（掉落由 onDestroyedByPlayer 或 getDrops 处理）
        if (base == BaseType.POLE) {
            TrafficSignalPoleBlock poleBlock = (TrafficSignalPoleBlock) NcbRoadblckModBlocks.TRAFFIC_SIGNAL_POLE.get();
            BlockState poleState = poleBlock.calculateState(level, pos);
            level.setBlock(pos, poleState, 3);
        } else if (base == BaseType.SMALLPOLE) {
            TrafficSignalSmallpoleBlock smallpoleBlock = (TrafficSignalSmallpoleBlock) NcbRoadblckModBlocks.TRAFFIC_SIGNAL_SMALLPOLE.get();
            TrafficSignalSmallpoleBlock.PoleType type = smallpoleBlock.getPoleType(level, pos);
            BlockState smallpoleState = smallpoleBlock.defaultBlockState()
                .setValue(TrafficSignalSmallpoleBlock.POLE_TYPE, type);
            level.setBlock(pos, smallpoleState, 3);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BaseType base = state.getValue(BASE);
        if (base == BaseType.NONE) {
            int dir = ((state.getValue(ROTATION) / 4) + 2) % 4;
            return switch (dir) {
                case 0 -> SHAPE_SOUTH;
                case 1 -> SHAPE_WEST;
                case 2 -> SHAPE_NORTH;
                case 3 -> SHAPE_EAST;
                default -> SHAPE_SOUTH;
            };
        } else if (base == BaseType.POLE) {
            return getPoleShape();
        } else {
            return getSmallpoleShape();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    protected abstract VoxelShape getPoleShape();
    protected abstract VoxelShape getSmallpoleShape();

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
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        // 用于爆炸、活塞等非玩家破坏场景
        List<ItemStack> drops = new ArrayList<>();
        int stage = state.getValue(STAGE);
        drops.add(new ItemStack(this.asItem(), stage));
        return drops;
    }

    public BaseType getBaseType(BlockState state) {
        return state.getValue(BASE);
    }

    public static int playerYawToRotation(float yRot) {
        return Mth.floor((-yRot) * 16.0 / 360.0 + 0.5) & 15;
    }
}