package net.flyaway.ncbroadblck.item;

import net.flyaway.ncbroadblck.client.ElectricWrenchRenderer;
import net.flyaway.ncbroadblck.init.NcbRoadblckModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
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

            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            tag.putLong("LastUseTick", level.getGameTime());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            player.startUsingItem(hand);

            if (player.isShiftKeyDown()) {
                onShiftRightClick(player, stack);
            } else {
                onRightClick(player, stack);
            }
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
    }

    protected void onRightClick(Player player, ItemStack stack) {
        // TODO: 仅右键功能
    }

    protected void onShiftRightClick(Player player, ItemStack stack) {
        // TODO: Shift+右键功能
    }
}