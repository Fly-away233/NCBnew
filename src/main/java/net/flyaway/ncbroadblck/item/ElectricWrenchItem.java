package net.flyaway.ncbroadblck.item;

import net.flyaway.ncbroadblck.block.ManholeCoverBlock;
import net.flyaway.ncbroadblck.block.RoadsignBlock;
import net.flyaway.ncbroadblck.client.ElectricWrenchRenderer;
import net.flyaway.ncbroadblck.init.NcbRoadblckModBlocks;
import net.flyaway.ncbroadblck.init.NcbRoadblckModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ElectricWrenchItem extends Item implements GeoItem {

    private static final RawAnimation NORMAL_ANIM = RawAnimation.begin().thenLoop("normal");
    private static final RawAnimation USE_ANIM = RawAnimation.begin().thenPlay("use");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ElectricWrenchItem() {
        super(new Item.Properties().stacksTo(1));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 10;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    @SuppressWarnings("removal")
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ElectricWrenchRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ElectricWrenchRenderer();
                }
                return this.renderer;
            }

            // ==========================================
            // 修复：移除第一人称判断，始终返回 CROSSBOW_HOLD
            // 这样不会抽搐，第一人称和第三人称都稳定持弩
            // ==========================================
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                // 情况1：正在使用物品中（按住右键时）
                if (entity.isUsingItem()
                        && entity.getUseItemRemainingTicks() > 0
                        && entity.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                // 情况2：松开右键后，根据 NBT 时间戳继续保持 0.5 秒
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                if (tag.contains("LastUseTick", Tag.TAG_LONG)) {
                    long lastUseTick = tag.getLong("LastUseTick");
                    long currentTick = entity.level().getGameTime();
                    if (currentTick - lastUseTick < 10) {
                        return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                    }
                }

                return null; // 默认姿势
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, state -> {
            state.getController().setAnimation(NORMAL_ANIM);
            return PlayState.CONTINUE;
        }).triggerableAnim("use", USE_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ElectricWrenchRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ElectricWrenchRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "main_controller", "use");

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                NcbRoadblckModSounds.ELECTRIC_WRENCH_USE.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

            markLastUse(stack, level);

            player.startUsingItem(hand);
        }

        return InteractionResultHolder.consume(stack);
    }

    /**
     * 对着方块右键：
     * 1. Shift+右键 -> 拆除 road_facilities 创造标签页中的方块及所有 RoadsignBlock 子类方块（等效原版破坏），掉落物直接进背包
     * 2. 右键 -> 循环改变名称带 roadsign 的方块的 rotation（base 为 none 时不能旋转）
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return InteractionResult.PASS;
        }

        // Shift+右键：拆除 road_facilities 标签页的方块及所有路牌方块
        if (player.isShiftKeyDown()) {
            if (!canWrenchBreak(state.getBlock())) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel) {
                wrenchBreak(serverLevel, pos, state, player, context.getItemInHand());
            }
            playUseFeedback(player, context.getItemInHand(), level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // 右键：切换井盖开关状态（模型切换 + 碰撞箱切换 + 铜门音效）
        if (state.getBlock() instanceof ManholeCoverBlock) {
            if (level instanceof ServerLevel serverLevel) {
                ManholeCoverBlock.toggleOpen(state, level, pos, player);
                playUseFeedback(player, context.getItemInHand(), level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // 右键：循环旋转路牌
        String blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (!blockName.contains("roadsign") || !state.hasProperty(RoadsignBlock.ROTATION)) {
            return InteractionResult.PASS;
        }
        // base 为 none 时不能旋转
        if (state.hasProperty(RoadsignBlock.BASE)
                && state.getValue(RoadsignBlock.BASE) == RoadsignBlock.BaseType.NONE) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel serverLevel) {
            level.setBlock(pos, state.cycle(RoadsignBlock.ROTATION), 3);
            playUseFeedback(player, context.getItemInHand(), level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
    }

    // 可被扳机拆除的方块：road_facilities 标签页方块 + 所有 RoadsignBlock 子类 + ManholeCoverBlock
    private static boolean canWrenchBreak(Block block) {
        return block instanceof RoadsignBlock
                || block instanceof ManholeCoverBlock
                || roadFacilitiesBlocks().contains(block);
    }

    // road_facilities 创造标签页中的方块集合，需与 NcbRoadblckModTabs.ROAD_FACILITIES 保持一致
    private static volatile Set<Block> roadFacilitiesBlocksCache;

    private static Set<Block> roadFacilitiesBlocks() {
        Set<Block> blocks = roadFacilitiesBlocksCache;
        if (blocks == null) {
            blocks = Set.of(
                    NcbRoadblckModBlocks.BOLLARD_SIDEWALK.get(),
                    NcbRoadblckModBlocks.STONE_PILLAR.get(),
                    NcbRoadblckModBlocks.SPEED_BUMP.get(),
                    NcbRoadblckModBlocks.GUARDRAIL.get(),
                    NcbRoadblckModBlocks.GUARDRAIL_SMALL.get(),
                    NcbRoadblckModBlocks.TRAFFIC_SIGNAL_POLE.get(),
                    NcbRoadblckModBlocks.TRAFFIC_SIGNAL_MAST.get(),
                    NcbRoadblckModBlocks.TRAFFIC_SIGNAL_SMALLPOLE.get(),
                    NcbRoadblckModBlocks.MANHOLE_COVER.get(),
					NcbRoadblckModBlocks.WHEEL_STOP.get(),
					NcbRoadblckModBlocks.SIGN_LANE_TURN_LEFT.get(),
					NcbRoadblckModBlocks.SIGN_LANE_STRAIGHT.get(),
					NcbRoadblckModBlocks.SIGN_LANE_TURN_RIGHT.get(),
					NcbRoadblckModBlocks.SIGN_LANE_STRAIGHT_OR_TURN_LEFT.get(),
					NcbRoadblckModBlocks.SIGN_LANE_STRAIGHT_OR_TURN_RIGHT.get(),
					NcbRoadblckModBlocks.SIGN_LANE_UTURN.get(),
					NcbRoadblckModBlocks.SIGN_LANE_UTURN_OR_TURN_LEFT.get()
                    );
            roadFacilitiesBlocksCache = blocks;
        }
        return blocks;
    }

    // 等效于原版破坏：统计/饥饿（生存）+ 破坏特效 + 移除方块，掉落物直接进背包
    private void wrenchBreak(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack tool) {
        if (!player.isCreative()) {
            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
            player.causeFoodExhaustion(0.005F);
        }

        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);

        if (state.getBlock() instanceof RoadsignBlock) {
            // RoadsignBlock.onDestroyedByPlayer 会把掉落物丢到世界里，这里绕过它直接移除方块
            // 移除后其 onRemove 会自动复原杆方块
            level.levelEvent(2001, pos, Block.getId(state));
            level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), 3);
        } else {
            level.destroyBlock(pos, false, player);
        }

        for (ItemStack drop : drops) {
            player.getInventory().placeItemBackInInventory(drop);
        }
    }

    // 使用反馈：动画 + 音效 + 记录使用时间（供客户端手臂姿势使用）
    private void playUseFeedback(Player player, ItemStack stack, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "main_controller", "use");
            level.playSound(null, pos, NcbRoadblckModSounds.ELECTRIC_WRENCH_USE.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        markLastUse(stack, level);
    }

    private void markLastUse(ItemStack stack, Level level) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putLong("LastUseTick", level.getGameTime());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}