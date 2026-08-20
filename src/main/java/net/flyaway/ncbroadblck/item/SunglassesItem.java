package net.flyaway.ncbroadblck.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.function.Consumer;

public class SunglassesItem extends Item implements Equipable {

    public SunglassesItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    // ========== 1. 右键穿戴到头部 ==========
    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    // ========== 2. 穿戴音效（皮革甲）==========
    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    // ========== 3. 手持物品右键直接装备 ==========
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    // ========== 4. 名字粗体 ==========
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .withStyle(ChatFormatting.BOLD);
    }

    // ========== 5. Lore 信息文字 ==========
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
            Component.literal("T-800: \"Get down.\"")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // ========== 6. 第一人称视野遮挡（墨镜效果）==========
    @Override
    @SuppressWarnings("removal")
    public void initializeClient(Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() != this) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        GuiGraphics g = event.getGuiGraphics();

        // 全屏半透明黑色遮挡
        g.fill(0, 0, width, height, 0xA0000000);

        // 白块参数
        int dot = 16;
        int gap = 4;
        int step = dot + gap;
        // 40% 不透明度 (alpha = 0x66 ≈ 102/255)，即 60% 透明
        int white = 0x66FFFFFF;

        int marginX = width / 15;
        int marginY = height / 10;

        int lx = marginX;
        int ly = height - marginY;
        int rx = width - marginX - step * 5;
        int ry = height - marginY;

        // 左下角矩阵 [10100][01010][00101]
        drawDot(g, lx + step * 0, ly - step * 2, dot, white);
        drawDot(g, lx + step * 2, ly - step * 2, dot, white);
        drawDot(g, lx + step * 1, ly - step * 1, dot, white);
        drawDot(g, lx + step * 3, ly - step * 1, dot, white);
        drawDot(g, lx + step * 2, ly - step * 0, dot, white);
        drawDot(g, lx + step * 4, ly - step * 0, dot, white);

        // 右下角矩阵 [10100][01010][00101]
        drawDot(g, rx + step * 0, ry - step * 2, dot, white);
        drawDot(g, rx + step * 2, ry - step * 2, dot, white);
        drawDot(g, rx + step * 1, ry - step * 1, dot, white);
        drawDot(g, rx + step * 3, ry - step * 1, dot, white);
        drawDot(g, rx + step * 2, ry - step * 0, dot, white);
        drawDot(g, rx + step * 4, ry - step * 0, dot, white);
    }

    private void drawDot(GuiGraphics g, int x, int y, int size, int color) {
        g.fill(x, y, x + size, y + size, color);
    }
}