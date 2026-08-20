package net.flyaway.ncbroadblck.block.entity;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class SlashModelData {
    public static final ModelProperty<BlockState> BELOW_STATE = new ModelProperty<>();
    public static final ModelProperty<Boolean> HAS_SOLID_BELOW = new ModelProperty<>();
}