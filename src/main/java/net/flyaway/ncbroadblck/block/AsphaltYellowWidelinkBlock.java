package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class AsphaltYellowWidelinkBlock extends AsphaltWhiteWidelinkBlock {

    public AsphaltYellowWidelinkBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .sound(SoundType.BASALT)
            .strength(1.5f, 10f)
            .requiresCorrectToolForDrops());
    }
}